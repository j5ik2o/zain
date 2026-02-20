package zain.core.mcp

import org.scalatest.funsuite.AnyFunSuite

final class McpClientSpec extends AnyFunSuite:
  test("should return McpResult for openSession listTools callTool ping closeSession"):
    val client = new StubMcpClient
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3000")
    val sessionId = McpSessionId("session-1")

    val openResult = client.openSession(config)
    val listResult = client.listTools(sessionId)
    val callResult = client.callTool(
      sessionId,
      McpCallToolRequest("echo", Map("msg" -> McpCallToolArgument.StringValue("hi")))
    )
    val pingResult = client.ping(sessionId)
    val closeResult = client.closeSession(sessionId)

    assert(openResult == McpResult.Success(sessionId))
    assert(listResult == McpResult.Success(Seq(McpToolInfo("echo", Some("echo tool")))))
    assert(callResult == McpResult.Success(McpCallToolResult(Seq(McpContent.Text("hi")), isError = false)))
    assert(pingResult == McpResult.Success(()))
    assert(closeResult == McpResult.Success(()))

private final class StubMcpClient extends McpClient:
  override def openSession(config: McpConnectionConfig): McpResult[McpSessionId] =
    McpResult.Success(McpSessionId("session-1"))

  override def listTools(sessionId: McpSessionId): McpResult[Seq[McpToolInfo]] =
    McpResult.Success(Seq(McpToolInfo("echo", Some("echo tool"))))

  override def callTool(sessionId: McpSessionId, request: McpCallToolRequest): McpResult[McpCallToolResult] =
    McpResult.Success(McpCallToolResult(Seq(McpContent.Text("hi")), isError = false))

  override def ping(sessionId: McpSessionId): McpResult[Unit] =
    McpResult.Success(())

  override def closeSession(sessionId: McpSessionId): McpResult[Unit] =
    McpResult.Success(())
