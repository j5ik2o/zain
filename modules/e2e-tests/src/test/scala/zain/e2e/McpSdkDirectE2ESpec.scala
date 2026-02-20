package zain.e2e

import scala.jdk.CollectionConverters.*
import scala.util.Try

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.ServerParameters
import io.modelcontextprotocol.client.transport.StdioClientTransport
import io.modelcontextprotocol.json.McpJsonDefaults
import io.modelcontextprotocol.json.McpJsonMapper
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper
import org.scalatest.funsuite.AnyFunSuite
import tools.jackson.core.JsonToken
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.DeserializationProblemHandler

final class McpSdkDirectE2ESpec extends AnyFunSuite:
  private def commandExists(command: String): Boolean =
    Try(Runtime.getRuntime.exec(Array("which", command)).waitFor() == 0).getOrElse(false)

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

  private def connectViaStdio(command: String, args: String*): McpSyncClient =
    val params = ServerParameters.builder(command).args(args.toList.asJava).build()
    val transport = new StdioClientTransport(params, lenientMapper())
    McpClient.sync(transport).build()

  private def withClient(command: String, args: String*)(body: McpSyncClient => Unit): Unit =
    val client = connectViaStdio(command, args*)
    try
      body(client)
    finally
      Try(client.closeGracefully())
      Try(client.close())

  // --- Codex MCP Server ---

  test("Codex: should initialize, ping, and close"):
    assume(commandExists("codex"), "codex CLI is not installed")

    withClient("codex", "mcp-server"): client =>
      val initResult = client.initialize()
      assert(initResult != null, "initialize must return a result")
      assert(initResult.serverInfo() != null, "server info must be present")
      info(s"Codex server: ${initResult.serverInfo().name()} v${initResult.serverInfo().version()}")

      client.ping()

  test("Codex: should list tools"):
    assume(commandExists("codex"), "codex CLI is not installed")

    withClient("codex", "mcp-server"): client =>
      client.initialize()

      val tools = client.listTools()
      assert(tools != null, "listTools must return a result")
      assert(tools.tools().size() > 0, "Codex must expose at least one tool")
      info(s"Codex MCP server provides ${tools.tools().size()} tool(s):")
      tools.tools().asScala.foreach(t => info(s"  - ${t.name()}: ${t.description()}"))

  // --- Claude Code MCP Server ---

  test("Claude Code: should initialize, ping, and close"):
    assume(commandExists("claude"), "claude CLI is not installed")

    withClient("claude", "mcp", "serve"): client =>
      val initResult = client.initialize()
      assert(initResult != null, "initialize must return a result")
      assert(initResult.serverInfo() != null, "server info must be present")
      info(s"Claude Code server: ${initResult.serverInfo().name()} v${initResult.serverInfo().version()}")

      client.ping()

  test("Claude Code: should list tools"):
    assume(commandExists("claude"), "claude CLI is not installed")

    withClient("claude", "mcp", "serve"): client =>
      client.initialize()

      val tools = client.listTools()
      assert(tools != null, "listTools must return a result")
      assert(tools.tools().size() > 0, "Claude Code must expose at least one tool")
      info(s"Claude Code MCP server provides ${tools.tools().size()} tool(s):")
      tools.tools().asScala.foreach(t => info(s"  - ${t.name()}: ${t.description()}"))
