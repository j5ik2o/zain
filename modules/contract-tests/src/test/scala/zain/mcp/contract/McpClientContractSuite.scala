package zain.mcp.contract

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpCallToolArgument
import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpClient
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpResult
import zain.core.mcp.McpSessionId
import zain.core.mcp.McpToolInfo

abstract class McpClientContractSuite extends AnyFunSuite:
  protected def createClient(): McpClient

  protected def availableConfig: McpConnectionConfig

  protected def unavailableConfig: McpConnectionConfig

  test("should open session successfully when connection is available"):
    val client = createClient()

    val result = client.openSession(availableConfig)

    result match
      case McpResult.Success(sessionId) =>
        assert(sessionId.value.nonEmpty)
      case failure =>
        fail(s"openSession must succeed but got: $failure")

  test("should return ConnectionUnavailable when opening unavailable connection"):
    val client = createClient()

    val result = client.openSession(unavailableConfig)

    assert(result == McpResult.Failure(McpError.ConnectionUnavailable))

  test("should list tools on opened session"):
    val client = createClient()
    val sessionId = openSessionOrFail(client)

    val result = client.listTools(sessionId)

    result match
      case McpResult.Success(tools) =>
        assert(tools.nonEmpty)
      case failure =>
        fail(s"listTools must succeed but got: $failure")

  test("should call tool on opened session"):
    val client = createClient()
    val sessionId = openSessionOrFail(client)

    val result = client.callTool(
      sessionId,
      McpCallToolRequest("echo", Map("msg" -> McpCallToolArgument.StringValue("hello")))
    )

    result match
      case McpResult.Success(toolResult) =>
        assert(toolResult.content.nonEmpty)
      case failure =>
        fail(s"callTool must succeed but got: $failure")

  test("should return InvalidRequest when calling tool with empty name"):
    val client = createClient()
    val sessionId = openSessionOrFail(client)

    val result = client.callTool(sessionId, McpCallToolRequest(""))

    assert(result == McpResult.Failure(McpError.InvalidRequest))

  test("should ping opened session"):
    val client = createClient()
    val sessionId = openSessionOrFail(client)

    val result = client.ping(sessionId)

    assert(result == McpResult.Success(()))

  test("should return SessionNotFound when listing tools on unopened session"):
    val client = createClient()

    val result = client.listTools(McpSessionId("missing"))

    assert(result == McpResult.Failure(McpError.SessionNotFound))

  test("should return SessionClosed when listing tools after session is closed"):
    val client = createClient()
    val sessionId = openSessionOrFail(client)

    val closeResult = client.closeSession(sessionId)
    val listResult = client.listTools(sessionId)

    assert(closeResult == McpResult.Success(()))
    assert(listResult == McpResult.Failure(McpError.SessionClosed))

  test("should return SessionNotFound when closing unopened session"):
    val client = createClient()

    val result = client.closeSession(McpSessionId("missing"))

    assert(result == McpResult.Failure(McpError.SessionNotFound))

  private def openSessionOrFail(client: McpClient): McpSessionId =
    client.openSession(availableConfig) match
      case McpResult.Success(sessionId) => sessionId
      case failure                      => fail(s"openSession must succeed in setup but got: $failure")
