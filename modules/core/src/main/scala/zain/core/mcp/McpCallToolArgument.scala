package zain.core.mcp

import scala.jdk.CollectionConverters.*

enum McpCallToolArgument:
  case NullValue
  case StringValue(value: String)
  case BooleanValue(value: Boolean)
  case NumberValue(value: BigDecimal)
  case ObjectValue(fields: Map[String, McpCallToolArgument])
  case ArrayValue(items: Seq[McpCallToolArgument])

  private[mcp] def toJavaObject: java.lang.Object = this match
    case McpCallToolArgument.NullValue => null
    case McpCallToolArgument.StringValue(value) => value
    case McpCallToolArgument.BooleanValue(value) => java.lang.Boolean.valueOf(value)
    case McpCallToolArgument.NumberValue(value) => value.bigDecimal
    case McpCallToolArgument.ObjectValue(fields) =>
      val converted = fields.map { case (key, argument) =>
        key -> argument.toJavaObject
      }
      converted.asJava
    case McpCallToolArgument.ArrayValue(items) =>
      val converted = items.map(_.toJavaObject)
      converted.asJava
