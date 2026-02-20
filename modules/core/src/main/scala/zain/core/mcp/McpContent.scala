package zain.core.mcp

sealed trait McpContent

object McpContent:
  final case class Text(text: String) extends McpContent
  final case class Image(data: String, mimeType: String) extends McpContent
  final case class Audio(data: String, mimeType: String) extends McpContent
