package zain.mcp.sdk

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpCallToolArgument
import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpProvider
import zain.core.mcp.McpResult
import zain.core.mcp.McpSessionCatalog
import zain.core.mcp.McpSessionId
import zain.core.mcp.McpToolInfo

final class McpSdkClientAdapterSpec extends AnyFunSuite:
  test("should open and close session when connection is available"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))

    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val opened = adapter.openSession(config)
    val closed = opened match
      case McpResult.Success(sessionId) => adapter.closeSession(sessionId)
      case _                            => fail("openSession must succeed in this test")

    assert(opened.isInstanceOf[McpResult.Success[McpSessionId]])
    assert(closed == McpResult.Success(()))

  test("should return ConnectionUnavailable when openSession fails"):
    val adapter = newAdapter(openResult = Left(McpError.ConnectionUnavailable))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")

    val actual = adapter.openSession(config)

    assert(actual == McpResult.Failure(McpError.ConnectionUnavailable))

  test("should list tools on opened session"):
    val tools = Seq(McpToolInfo("echo", Some("echo tool")))
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle(listToolsResult = Right(tools))))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val actual = adapter.listTools(sessionId)

    assert(actual == McpResult.Success(tools))

  test("should call tool on opened session"):
    val result = McpCallToolResult(Seq(McpContent.Text("hello")), isError = false)
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle(callToolResult = Right(result))))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val actual = adapter.callTool(sessionId, McpCallToolRequest("echo", Map("msg" -> McpCallToolArgument.StringValue("hello"))))

    assert(actual == McpResult.Success(result))

  test("should return InvalidRequest when calling tool with empty name"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val actual = adapter.callTool(sessionId, McpCallToolRequest(""))

    assert(actual == McpResult.Failure(McpError.InvalidRequest))

  test("should ping opened session"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val actual = adapter.ping(sessionId)

    assert(actual == McpResult.Success(()))

  test("should return SessionNotFound when listing tools on unknown session"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))

    val actual = adapter.listTools(McpSessionId("missing"))

    assert(actual == McpResult.Failure(McpError.SessionNotFound))

  test("should return SessionClosed when listing tools after closeSession"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val closeResult = adapter.closeSession(sessionId)
    val actual = adapter.listTools(sessionId)

    assert(closeResult == McpResult.Success(()))
    assert(actual == McpResult.Failure(McpError.SessionClosed))

  test("should return ConnectionUnavailable when listTools handle fails"):
    val adapter = newAdapter(
      openResult = Right(StubMcpSdkClientHandle(listToolsResult = Left(McpError.ConnectionUnavailable)))
    )
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val actual = adapter.listTools(sessionId)

    assert(actual == McpResult.Failure(McpError.ConnectionUnavailable))

  test("should keep session opened when close handle fails"):
    val adapter = newAdapter(
      openResult = Right(StubMcpSdkClientHandle(closeResult = Left(McpError.ConnectionUnavailable)))
    )
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val closeResult = adapter.closeSession(sessionId)
    val pingAfterFailedClose = adapter.ping(sessionId)

    assert(closeResult == McpResult.Failure(McpError.ConnectionUnavailable))
    assert(pingAfterFailedClose == McpResult.Success(()))

  test("should return SessionNotFound when closing unknown session"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))

    val actual = adapter.closeSession(McpSessionId("missing"))

    assert(actual == McpResult.Failure(McpError.SessionNotFound))

  test("should return SessionClosed when closing already closed session"):
    val adapter = newAdapter(openResult = Right(StubMcpSdkClientHandle()))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val sessionId = openSessionOrFail(adapter, config)

    val firstClose = adapter.closeSession(sessionId)
    val secondClose = adapter.closeSession(sessionId)

    assert(firstClose == McpResult.Success(()))
    assert(secondClose == McpResult.Failure(McpError.SessionClosed))

  private def newAdapter(
      openResult: Either[McpError.ConnectionUnavailable.type, McpSdkClientHandle]
  ): McpSdkClientAdapter =
    val transportFactory = new McpSdkTransportFactory(_ => openResult)
    new McpSdkClientAdapter(McpProvider.Codex, transportFactory, new McpSessionCatalog)

  private def openSessionOrFail(adapter: McpSdkClientAdapter, config: McpConnectionConfig): McpSessionId =
    adapter.openSession(config) match
      case McpResult.Success(sessionId) => sessionId
      case failure                      => fail(s"openSession must succeed in setup but got: $failure")

private final case class StubMcpSdkClientHandle(
    listToolsResult: Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
      Right(Seq(McpToolInfo("stub", None))),
    callToolResult: Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
      Right(McpCallToolResult(Seq(McpContent.Text("stub")), isError = false)),
    pingResult: Either[McpError.ConnectionUnavailable.type, Unit] = Right(()),
    closeResult: Either[McpError.ConnectionUnavailable.type, Unit] = Right(())
) extends McpSdkClientHandle:
  override def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
    listToolsResult

  override def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
    callToolResult

  override def ping(): Either[McpError.ConnectionUnavailable.type, Unit] =
    pingResult

  override def close(): Either[McpError.ConnectionUnavailable.type, Unit] =
    closeResult
