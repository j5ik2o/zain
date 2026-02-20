package zain.mcp.claude

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpCallToolArgument
import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpResult
import zain.core.mcp.McpSessionId
import zain.core.mcp.McpToolInfo

final class ClaudeCodeMcpClientAdapterSpec extends AnyFunSuite:
  private val validConfig = McpConnectionConfig.StreamableHttp("http://localhost:3000")

  test("should open session successfully"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))

    val result = client.openSession(validConfig)

    result match
      case McpResult.Success(sessionId) =>
        assert(sessionId.value.nonEmpty)
      case failure =>
        fail(s"expected session open success but got: $failure")

  test("should return ConnectionUnavailable when opening unavailable server"):
    val client = newAdapter(openResult = Left(McpError.ConnectionUnavailable))

    val result = client.openSession(validConfig)

    assert(result == McpResult.Failure(McpError.ConnectionUnavailable))

  test("should list tools on opened session"):
    val tools = Seq(McpToolInfo("claude-tool", Some("a tool")))
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle(listToolsResult = Right(tools))))
    val sessionId = openSessionOrFail(client)

    val result = client.listTools(sessionId)

    assert(result == McpResult.Success(tools))

  test("should call tool on opened session"):
    val toolResult = McpCallToolResult(Seq(McpContent.Text("result")), isError = false)
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle(callToolResult = Right(toolResult))))
    val sessionId = openSessionOrFail(client)

    val result = client.callTool(sessionId, McpCallToolRequest("echo", Map("msg" -> McpCallToolArgument.StringValue("a"))))

    assert(result == McpResult.Success(toolResult))

  test("should return InvalidRequest when calling tool with empty name"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))
    val sessionId = openSessionOrFail(client)

    val result = client.callTool(sessionId, McpCallToolRequest(""))

    assert(result == McpResult.Failure(McpError.InvalidRequest))

  test("should ping opened session"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))
    val sessionId = openSessionOrFail(client)

    val result = client.ping(sessionId)

    assert(result == McpResult.Success(()))

  test("should return SessionNotFound when listing tools on unknown session"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))

    val result = client.listTools(McpSessionId("unknown"))

    assert(result == McpResult.Failure(McpError.SessionNotFound))

  test("should return SessionClosed when listing tools after close"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))
    val sessionId = openSessionOrFail(client)
    val closeResult = client.closeSession(sessionId)

    val result = client.listTools(sessionId)

    assert(closeResult == McpResult.Success(()))
    assert(result == McpResult.Failure(McpError.SessionClosed))

  test("should return ConnectionUnavailable when listTools handle fails"):
    val client = newAdapter(
      openResult = Right(StubClaudeCodeClientHandle(listToolsResult = Left(McpError.ConnectionUnavailable)))
    )
    val sessionId = openSessionOrFail(client)

    val result = client.listTools(sessionId)

    assert(result == McpResult.Failure(McpError.ConnectionUnavailable))

  test("should keep session opened when close handle fails"):
    val client = newAdapter(
      openResult = Right(StubClaudeCodeClientHandle(closeResult = Left(McpError.ConnectionUnavailable)))
    )
    val sessionId = openSessionOrFail(client)

    val closeResult = client.closeSession(sessionId)
    val pingAfterFailedClose = client.ping(sessionId)

    assert(closeResult == McpResult.Failure(McpError.ConnectionUnavailable))
    assert(pingAfterFailedClose == McpResult.Success(()))

  test("should return SessionNotFound when closing unknown session"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))

    val result = client.closeSession(McpSessionId("unknown"))

    assert(result == McpResult.Failure(McpError.SessionNotFound))

  test("should return SessionClosed when closing already closed session"):
    val client = newAdapter(openResult = Right(StubClaudeCodeClientHandle()))
    val sessionId = openSessionOrFail(client)

    val first = client.closeSession(sessionId)
    val second = client.closeSession(sessionId)

    assert(first == McpResult.Success(()))
    assert(second == McpResult.Failure(McpError.SessionClosed))

  private def newAdapter(
      openResult: Either[McpError.ConnectionUnavailable.type, ClaudeCodeClientHandle]
  ): ClaudeCodeMcpClientAdapter =
    val factory = new ClaudeCodeTransportFactory(_ => openResult)
    new ClaudeCodeMcpClientAdapter(factory)

  private def openSessionOrFail(client: ClaudeCodeMcpClientAdapter): McpSessionId =
    client.openSession(validConfig) match
      case McpResult.Success(sessionId) => sessionId
      case failure                      => fail(s"failed to open session in setup: $failure")

private final case class StubClaudeCodeClientHandle(
    listToolsResult: Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
      Right(Seq(McpToolInfo("stub", None))),
    callToolResult: Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
      Right(McpCallToolResult(Seq(McpContent.Text("stub")), isError = false)),
    pingResult: Either[McpError.ConnectionUnavailable.type, Unit] = Right(()),
    closeResult: Either[McpError.ConnectionUnavailable.type, Unit] = Right(())
) extends ClaudeCodeClientHandle:
  override def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
    listToolsResult

  override def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
    callToolResult

  override def ping(): Either[McpError.ConnectionUnavailable.type, Unit] =
    pingResult

  override def close(): Either[McpError.ConnectionUnavailable.type, Unit] =
    closeResult
