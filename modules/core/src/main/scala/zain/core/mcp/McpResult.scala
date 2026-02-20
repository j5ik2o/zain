package zain.core.mcp

sealed trait McpResult[+A]

object McpResult:
  final case class Success[A](value: A) extends McpResult[A]

  final case class Failure(error: McpError) extends McpResult[Nothing]
