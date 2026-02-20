package zain.e2e

import scala.util.Try

import org.scalatest.funsuite.AnyFunSuite
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpResult
import zain.mcp.claude.ClaudeCodeMcpClientAdapter
import zain.mcp.claude.ClaudeCodeTransportFactory

final class ClaudeCodeMcpE2ESpec extends AnyFunSuite:
  private def commandExists(command: String): Boolean =
    Try(Runtime.getRuntime.exec(Array("which", command)).waitFor() == 0).getOrElse(false)

  private val claudeConfig = McpConnectionConfig.Stdio(
    command = "claude",
    args = Seq("mcp", "serve")
  )

  test("should open session to real Claude Code MCP server"):
    assume(commandExists("claude"), "claude CLI is not installed")

    val adapter = new ClaudeCodeMcpClientAdapter(new ClaudeCodeTransportFactory)
    val result = adapter.openSession(claudeConfig)

    result match
      case McpResult.Success(sessionId) =>
        assert(sessionId.value.nonEmpty)
        info(s"Claude Code session opened: ${sessionId.value}")
        val closeResult = adapter.closeSession(sessionId)
        assert(closeResult == McpResult.Success(()), s"close failed: $closeResult")
      case McpResult.Failure(error) =>
        fail(s"Failed to open Claude Code MCP session: $error")

  test("should list tools on real Claude Code MCP server"):
    assume(commandExists("claude"), "claude CLI is not installed")

    val adapter = new ClaudeCodeMcpClientAdapter(new ClaudeCodeTransportFactory)
    val opened = adapter.openSession(claudeConfig)

    opened match
      case McpResult.Success(sessionId) =>
        info(s"Claude Code session opened: ${sessionId.value}")

        val listResult = adapter.listTools(sessionId)
        listResult match
          case McpResult.Success(tools) =>
            info(s"Claude Code tools (${tools.size}): ${tools.map(_.name).mkString(", ")}")
            assert(tools.nonEmpty, "Claude Code should expose at least one tool")
          case McpResult.Failure(error) =>
            fail(s"listTools failed: $error")

        val pingResult = adapter.ping(sessionId)
        assert(pingResult == McpResult.Success(()), s"ping failed: $pingResult")

        val closeResult = adapter.closeSession(sessionId)
        assert(closeResult == McpResult.Success(()), s"close failed: $closeResult")
      case McpResult.Failure(error) =>
        fail(s"Failed to open Claude Code MCP session: $error")

  test("should return ConnectionUnavailable for non-existent Claude Code server"):
    val badConfig = McpConnectionConfig.Stdio(
      command = "claude-nonexistent-command-e2e",
      args = Seq("mcp", "serve")
    )
    val adapter = new ClaudeCodeMcpClientAdapter(new ClaudeCodeTransportFactory)
    val result = adapter.openSession(badConfig)

    assert(result.isInstanceOf[McpResult.Failure])
