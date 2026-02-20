package zain.mcp.contract

import zain.core.mcp.McpCallToolRequest
import zain.core.mcp.McpCallToolResult
import zain.core.mcp.McpClient
import zain.core.mcp.McpConnectionConfig
import zain.core.mcp.McpContent
import zain.core.mcp.McpError
import zain.core.mcp.McpProvider
import zain.core.mcp.McpSessionCatalog
import zain.core.mcp.McpToolInfo
import zain.mcp.sdk.McpSdkClientAdapter
import zain.mcp.sdk.McpSdkClientHandle
import zain.mcp.sdk.McpSdkTransportFactory

final class CodexMcpClientContractSuite extends McpClientContractSuite:
  override protected def createClient(): McpClient =
    val unavailable = unavailableConfig
    val transportFactory = new McpSdkTransportFactory(config =>
      if config == unavailable then
        Left(McpError.ConnectionUnavailable)
      else
        Right(ContractCodexSessionHandle)
    )
    new McpSdkClientAdapter(McpProvider.Codex, transportFactory, new McpSessionCatalog)

  override protected def availableConfig: McpConnectionConfig =
    McpConnectionConfig.StreamableHttp("http://localhost:3100")

  override protected def unavailableConfig: McpConnectionConfig =
    McpConnectionConfig.StreamableHttp("http://localhost:3199")

private object ContractCodexSessionHandle extends McpSdkClientHandle:
  override def listTools(): Either[McpError.ConnectionUnavailable.type, Seq[McpToolInfo]] =
    Right(Seq(McpToolInfo("echo", Some("echo tool"))))

  override def callTool(request: McpCallToolRequest): Either[McpError.ConnectionUnavailable.type, McpCallToolResult] =
    Right(McpCallToolResult(Seq(McpContent.Text(s"echo:${request.name}")), isError = false))

  override def ping(): Either[McpError.ConnectionUnavailable.type, Unit] =
    Right(())

  override def close(): Either[McpError.ConnectionUnavailable.type, Unit] =
    Right(())
