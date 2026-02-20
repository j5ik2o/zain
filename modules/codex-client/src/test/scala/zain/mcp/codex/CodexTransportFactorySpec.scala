package zain.mcp.codex

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpToolInfo

final class CodexTransportFactorySpec extends AnyFunSuite:
  import CodexTransportFactory.CodexSessionHandle

  test("should create handle when session opener succeeds"):
    val expectedHandle = FactorySpecStubCodexSessionHandle()
    val factory = new CodexTransportFactory(_ => Right(expectedHandle))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")

    val actual = factory.create(config)

    assert(actual == Right(expectedHandle))

  test("should return ConnectionUnavailable when session opener fails"):
    val factory = new CodexTransportFactory(_ => Left(McpError.ConnectionUnavailable))
    val config = McpConnectionConfig.Sse("ignored")

    val actual = factory.create(config)

    assert(actual == Left(McpError.ConnectionUnavailable))

  test("should return ConnectionUnavailable when sdk initialize fails"):
    val factory = new CodexTransportFactory()
    val config = McpConnectionConfig.Stdio(
      command = "zain-mcp-test-command-that-should-not-exist-codex"
    )

    val actual = factory.create(config)

    assert(actual == Left(McpError.ConnectionUnavailable))

  test("should list tools and close through returned handle"):
    val expectedHandle = FactorySpecStubCodexSessionHandle()
    val factory = new CodexTransportFactory(_ => Right(expectedHandle))
    val config = McpConnectionConfig.StreamableHttp("http://localhost:3100")
    val created = factory.create(config)

    val listResult = created.flatMap(_.listTools())
    val closeResult = created.flatMap(_.close())

    assert(listResult == Right(Seq(McpToolInfo("stub", None))))
    assert(closeResult == Right(()))

private final case class FactorySpecStubCodexSessionHandle(
    listToolsResult: Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
      Right(Seq(McpToolInfo("stub", None))),
    callToolResult: Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
      Right(McpCallToolResult(Seq(McpContent.Text("stub")), isError = false)),
    pingResult: Either[McpError.ConnectionUnavailable.type, Unit] = Right(()),
    closeResult: Either[McpError.ConnectionUnavailable.type, Unit] = Right(())
) extends CodexTransportFactory.CodexSessionHandle:
  override def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
    listToolsResult

  override def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
    callToolResult

  override def ping(): Either[McpError.ConnectionUnavailable.type, Unit] =
    pingResult

  override def close(): Either[McpError.ConnectionUnavailable.type, Unit] =
    closeResult
