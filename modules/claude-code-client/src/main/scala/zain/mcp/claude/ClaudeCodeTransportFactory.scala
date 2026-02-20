package zain.mcp.claude

import scala.jdk.CollectionConverters.*
import scala.util.control.NonFatal

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.client.transport.ServerParameters
import io.modelcontextprotocol.client.transport.StdioClientTransport
import io.modelcontextprotocol.json.McpJsonDefaults
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper
import io.modelcontextprotocol.spec.McpClientTransport
import io.modelcontextprotocol.spec.McpSchema
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.DeserializationProblemHandler
import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpToolInfo

final class ClaudeCodeTransportFactory(
    sessionOpener: McpConnectionConfig => Either[McpError.ConnectionUnavailable.type, ClaudeCodeClientHandle]
):
  def this() = this(ClaudeCodeTransportFactory.openWithSdk)

  def create(config: McpConnectionConfig): Either[McpError.ConnectionUnavailable.type, ClaudeCodeClientHandle] =
    sessionOpener(config)

object ClaudeCodeTransportFactory:
  private final case class SdkClaudeCodeClientHandle(client: McpSyncClient) extends ClaudeCodeClientHandle:
    override def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
      toConnectionUnavailable {
        val result = client.listTools()
        result.tools().asScala.toSeq.map(t => McpToolInfo(t.name(), Option(t.description())))
      }

    override def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
      toConnectionUnavailable {
        val sdkRequest = new McpSchema.CallToolRequest(request.name, request.toJavaArguments)
        val result = client.callTool(sdkRequest)
        McpCallToolResult(
          content = result.content().asScala.toSeq.map(toMcpContent),
          isError = Option(result.isError()).exists(_.booleanValue())
        )
      }

    override def ping(): Either[McpError.ConnectionUnavailable.type, Unit] =
      toConnectionUnavailable {
        client.ping()
        ()
      }

    override def close(): Either[McpError.ConnectionUnavailable.type, Unit] =
      toConnectionUnavailable {
        val closed = client.closeGracefully()
        if !closed then
          throw new RuntimeException("MCP client closeGracefully returned false")
      }

  private def toMcpContent(c: McpSchema.Content): McpContent = c match
    case t: McpSchema.TextContent  => McpContent.Text(t.text())
    case i: McpSchema.ImageContent => McpContent.Image(i.data(), i.mimeType())
    case a: McpSchema.AudioContent => McpContent.Audio(a.data(), a.mimeType())
    case other                     => McpContent.Text(other.toString)

  private def toConnectionUnavailable[A](
      block: => A
  ): Either[McpError.ConnectionUnavailable.type, A] =
    try Right(block)
    catch
      case NonFatal(_) => Left(McpError.ConnectionUnavailable)

  private def closeSilently(client: McpSyncClient): Unit =
    try client.close()
    catch
      case NonFatal(_) => ()

  private def lenientMapper(): McpJsonMapper =
    val base = McpJsonDefaults.getMapper().asInstanceOf[JacksonMcpJsonMapper]
    val customJsonMapper = base.getJsonMapper().rebuild()
      .addHandler(new DeserializationProblemHandler {
        override def handleUnexpectedToken(
            ctxt: DeserializationContext,
            targetType: JavaType,
            t: JsonToken,
            p: tools.jackson.core.JsonParser,
            failureMsg: String
        ): AnyRef =
          if targetType.getRawClass == classOf[java.lang.Boolean] && t == JsonToken.START_OBJECT then
            p.skipChildren()
            java.lang.Boolean.TRUE
          else DeserializationProblemHandler.NOT_HANDLED
      })
      .build()
    new JacksonMcpJsonMapper(customJsonMapper)

  private def toSdkTransport(config: McpConnectionConfig): McpClientTransport =
    config match
      case McpConnectionConfig.Stdio(command, args, env) =>
        val builder = ServerParameters.builder(command)
        if args.nonEmpty then builder.args(args.asJava)
        if env.nonEmpty then builder.env(env.asJava)
        new StdioClientTransport(builder.build(), lenientMapper())
      case McpConnectionConfig.StreamableHttp(serverUrl) =>
        HttpClientStreamableHttpTransport.builder(serverUrl).build()
      case McpConnectionConfig.Sse(serverUrl) =>
        HttpClientSseClientTransport.builder(serverUrl).build()

  def openWithSdk(
      config: McpConnectionConfig
  ): Either[McpError.ConnectionUnavailable.type, ClaudeCodeClientHandle] =
    toConnectionUnavailable {
      McpClient.sync(toSdkTransport(config)).build()
    } match
      case Left(error) => Left(error)
      case Right(client) =>
        toConnectionUnavailable(client.initialize()) match
          case Right(_) => Right(SdkClaudeCodeClientHandle(client))
          case Left(error) =>
            closeSilently(client)
            Left(error)
