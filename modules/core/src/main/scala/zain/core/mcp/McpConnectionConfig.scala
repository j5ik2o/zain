package zain.core.mcp

sealed trait McpConnectionConfig

object McpConnectionConfig:
  final case class Stdio(
      command: String,
      args: Seq[String] = Seq.empty,
      env: Map[String, String] = Map.empty
  ) extends McpConnectionConfig

  final case class StreamableHttp(serverUrl: String) extends McpConnectionConfig

  final case class Sse(serverUrl: String) extends McpConnectionConfig
