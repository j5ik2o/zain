package zain.core.mcp

trait McpClient:
  def openSession(config: McpConnectionConfig): McpResult[McpSessionId]

  def listTools(sessionId: McpSessionId): McpResult[Seq[McpToolInfo]]

  def callTool(sessionId: McpSessionId, request: McpCallToolRequest): McpResult[McpCallToolResult]

  def ping(sessionId: McpSessionId): McpResult[Unit]

  def closeSession(sessionId: McpSessionId): McpResult[Unit]
