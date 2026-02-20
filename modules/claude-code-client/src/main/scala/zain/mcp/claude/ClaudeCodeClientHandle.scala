package zain.mcp.claude

import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpError
import zain.core.mcp.McpToolInfo

trait ClaudeCodeClientHandle:
  def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]]

  def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult]

  def ping(): Either[McpError.ConnectionUnavailable.type, Unit]

  def close(): Either[McpError.ConnectionUnavailable.type, Unit]
