package zain.core.mcp

enum McpProvider(val value: String):
  case Codex extends McpProvider("codex")
  case ClaudeCode extends McpProvider("claude-code")
