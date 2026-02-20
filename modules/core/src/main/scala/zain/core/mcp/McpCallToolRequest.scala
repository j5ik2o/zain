package zain.core.mcp

import scala.jdk.CollectionConverters.*

final case class McpCallToolRequest(
    name: String,
    arguments: Map[String, McpCallToolArgument] = Map.empty
):
  def toJavaArguments: java.util.Map[String, java.lang.Object] =
    val converted = arguments.map { case (key, argument) =>
      key -> argument.toJavaObject
    }
    converted.asJava
