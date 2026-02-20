package zain.mcp.contract

import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpClient
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpToolInfo
import zain.mcp.claude.ClaudeCodeClientHandle
import zain.mcp.claude.ClaudeCodeMcpClientAdapter
import zain.mcp.claude.ClaudeCodeTransportFactory

final class ClaudeCodeMcpClientContractSuite extends McpClientContractSuite:
  override protected def createClient(): McpClient =
    val unavailable = unavailableConfig
    val factory = new ClaudeCodeTransportFactory(config =>
      if config == unavailable then
        Left(McpError.ConnectionUnavailable)
      else
        Right(ContractClaudeCodeClientHandle)
    )
    new ClaudeCodeMcpClientAdapter(factory)

  override protected def availableConfig: McpConnectionConfig =
    McpConnectionConfig.StreamableHttp("http://localhost:3000")

  override protected def unavailableConfig: McpConnectionConfig =
    McpConnectionConfig.StreamableHttp("http://localhost:3099")

private object ContractClaudeCodeClientHandle extends ClaudeCodeClientHandle:
  override def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
    Right(Seq(McpToolInfo("echo", Some("echo tool"))))

  override def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
    Right(McpCallToolResult(Seq(McpContent.Text(s"claude:${request.name}")), isError = false))

  override def ping(): Either[McpError.ConnectionUnavailable.type, Unit] =
    Right(())

  override def close(): Either[McpError.ConnectionUnavailable.type, Unit] =
    Right(())
