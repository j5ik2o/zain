package zain.core.takt.primitives

enum RuleCondition:
  case Plain(text: String)
  case Ai(text: String)
  case Aggregate(
      aggregateType: RuleCondition.AggregateType,
      conditions: Vector[String]
  )

  def breachEncapsulationOfRawValue: String =
    this match
      case RuleCondition.Plain(text) =>
        text
      case RuleCondition.Ai(text) =>
        s"""ai("$text")"""
      case RuleCondition.Aggregate(aggregateType, conditions) =>
        val serializedConditions = conditions.map(value => s""""$value"""").mkString(", ")
        aggregateType.keyword + s"($serializedConditions)"

  def isAiCondition: Boolean =
    this match
      case RuleCondition.Ai(_) => true
      case _                   => false

  def aiConditionText: Option[String] =
    this match
      case RuleCondition.Ai(text) => Some(text)
      case _                      => None

  def aggregateCondition: Option[(RuleCondition.AggregateType, Vector[String])] =
    this match
      case RuleCondition.Aggregate(aggregateType, conditions) =>
        Some((aggregateType, conditions))
      case _ =>
        None

object RuleCondition:
  enum AggregateType(val keyword: String):
    case All extends AggregateType("all")
    case Any extends AggregateType("any")

  private val AiRegex = """^ai\("(.+)"\)$""".r
  private val AggregateRegex = """^(all|any)\((.+)\)$""".r
  private val QuotedConditionRegex = """"([^"]+)"""".r

  def parse(value: String): Either[TaktPrimitiveError, RuleCondition] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyRuleCondition)
    else
      value match
        case AiRegex(aiText) =>
          Right(RuleCondition.Ai(aiText))
        case AggregateRegex(aggregateTypeText, aggregateArgs) =>
          parseAggregateCondition(
            aggregateTypeText = aggregateTypeText,
            aggregateArgs = aggregateArgs
          )
        case _ =>
          if looksLikeAiSyntax(value) || looksLikeAggregateSyntax(value) then
            Left(TaktPrimitiveError.InvalidRuleConditionSyntax)
          else Right(RuleCondition.Plain(value))

  private def parseAggregateCondition(
      aggregateTypeText: String,
      aggregateArgs: String
  ): Either[TaktPrimitiveError, RuleCondition] =
    parseAggregateType(aggregateTypeText)
      .flatMap: aggregateType =>
        parseAggregateConditions(aggregateArgs)
          .map: conditions =>
            RuleCondition.Aggregate(
              aggregateType = aggregateType,
              conditions = conditions
            )

  private def parseAggregateType(
      value: String
  ): Either[TaktPrimitiveError, AggregateType] =
    value match
      case AggregateType.All.keyword => Right(AggregateType.All)
      case AggregateType.Any.keyword => Right(AggregateType.Any)
      case _                         => Left(TaktPrimitiveError.InvalidRuleConditionSyntax)

  private def parseAggregateConditions(
      value: String
  ): Either[TaktPrimitiveError, Vector[String]] =
    val parsed = QuotedConditionRegex.findAllMatchIn(value).map(_.group(1)).toVector

    if parsed.isEmpty then Left(TaktPrimitiveError.InvalidRuleConditionSyntax)
    else Right(parsed)

  private def looksLikeAiSyntax(value: String): Boolean =
    value.startsWith("ai(") || value.startsWith("ai（")

  private def looksLikeAggregateSyntax(value: String): Boolean =
    value.startsWith("all(") || value.startsWith("any(")
