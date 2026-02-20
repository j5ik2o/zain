package zain.mcp.claude

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpToolInfo

final class ClaudeCodeTransportFactorySpec extends AnyFunSuite:
  test("should create handle when session opener succeeds"):
    val expectedHandle = FactorySpecStubClaudeCodeClientHandle()
    val factory = new ClaudeCodeTransportFactory(_ => Right(expectedHandle))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3000")

    val created = factory.create(config)

    assert(created == Right(expectedHandle))

  test("should return ConnectionUnavailable when session opener fails"):
    val factory = new ClaudeCodeTransportFactory(_ => Left(McpError.ConnectionUnavailable))
    val config = McpConnectionConfig.StreamableHttp("ignored")

    val created = factory.create(config)

    assert(created == Left(McpError.ConnectionUnavailable))

  test("should return ConnectionUnavailable when sdk initialize fails"):
    val factory = new ClaudeCodeTransportFactory()
    val config = McpConnectionConfig.Stdio(
      command = "zain-mcp-test-command-that-should-not-exist-claude"
    )

    val created = factory.create(config)

    assert(created == Left(McpError.ConnectionUnavailable))

  test("should list tools and close through returned handle"):
    val expectedHandle = FactorySpecStubClaudeCodeClientHandle()
    val factory = new ClaudeCodeTransportFactory(_ => Right(expectedHandle))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3000")
    val created = factory.create(config)

    val listResult = created.flatMap(_.listTools())
    val closeResult = created.flatMap(_.close())

    assert(listResult == Right(Seq(McpToolInfo("stub", None))))
    assert(closeResult == Right(()))

private final case class FactorySpecStubClaudeCodeClientHandle(
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
