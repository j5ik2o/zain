package zain.e2e

import scala.util.Try

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpResult
import zain.core.mcp.McpSessionCatalog
import zain.mcp.codex.CodexMcpClientAdapter
import zain.mcp.codex.CodexTransportFactory

final class CodexMcpE2ESpec extends AnyFunSuite:
  private def commandExists(command: String): Boolean =
    Try(Runtime.getRuntime.exec(Array("which", command)).waitFor() == 0).getOrElse(false)

  private val codexConfig = McpConnectionConfig.Stdio(
    command = "codex",
    args = Seq("mcp-server")
  )

  test("should open session to real Codex MCP server"):
    assume(commandExists("codex"), "codex CLI is not installed")

    val adapter = new CodexMcpClientAdapter(new CodexTransportFactory, new McpSessionCatalog)
    val result = adapter.openSession(codexConfig)

    result match
      case McpResult.Success(sessionId) =>
        assert(sessionId.value.nonEmpty)
        info(s"Codex session opened: ${sessionId.value}")
        val closeResult = adapter.closeSession(sessionId)
        assert(closeResult == McpResult.Success(()), s"close failed: $closeResult")
      case McpResult.Failure(error) =>
        fail(s"Failed to open Codex MCP session: $error")

  test("should list tools on real Codex MCP server"):
    assume(commandExists("codex"), "codex CLI is not installed")

    val adapter = new CodexMcpClientAdapter(new CodexTransportFactory, new McpSessionCatalog)
    val opened = adapter.openSession(codexConfig)

    opened match
      case McpResult.Success(sessionId) =>
        info(s"Codex session opened: ${sessionId.value}")

        val listResult = adapter.listTools(sessionId)
        listResult match
          case McpResult.Success(tools) =>
            info(s"Codex tools (${tools.size}): ${tools.map(_.name).mkString(", ")}")
            assert(tools.nonEmpty, "Codex should expose at least one tool")
          case McpResult.Failure(error) =>
            fail(s"listTools failed: $error")

        val pingResult = adapter.ping(sessionId)
        assert(pingResult == McpResult.Success(()), s"ping failed: $pingResult")

        val closeResult = adapter.closeSession(sessionId)
        assert(closeResult == McpResult.Success(()), s"close failed: $closeResult")
      case McpResult.Failure(error) =>
        fail(s"Failed to open Codex MCP session: $error")

  test("should return ConnectionUnavailable for non-existent Codex server"):
    val badConfig = McpConnectionConfig.Stdio(
      command = "codex-nonexistent-command-e2e",
      args = Seq("mcp-server")
    )
    val adapter = new CodexMcpClientAdapter(new CodexTransportFactory, new McpSessionCatalog)
    val result = adapter.openSession(badConfig)

    assert(result.isInstanceOf[McpResult.Failure])
