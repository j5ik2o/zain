package zain.core.takt.primitives

enum RuleCondition:
  case Plain(text: RuleConditionText)
  case Ai(text: RuleConditionText)
  case Aggregate(
      aggregateType: RuleCondition.AggregateType,
      conditions: RuleConditionTexts
  )

  def breachEncapsulationOfRawValue: String =
    this match
      case RuleCondition.Plain(text) =>
        text.value
      case RuleCondition.Ai(text) =>
        s"""ai("${text.value}")"""
      case RuleCondition.Aggregate(aggregateType, conditions) =>
        val serializedConditions = conditions.map(value => s""""${value.value}"""").mkString(", ")
        aggregateType.keyword + s"($serializedConditions)"

  def isAiCondition: Boolean =
    this match
      case RuleCondition.Ai(_) => true
      case _                   => false

  def aiConditionText: Option[String] =
    this match
      case RuleCondition.Ai(text) => Some(text.value)
      case _                      => None

  def aggregateCondition: Option[(RuleCondition.AggregateType, Vector[String])] =
    this match
      case RuleCondition.Aggregate(aggregateType, conditions) =>
        Some((aggregateType, conditions.map(_.value)))
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
          RuleConditionText.parse(aiText).map(RuleCondition.Ai.apply)
        case AggregateRegex(aggregateTypeText, aggregateArgs) =>
          parseAggregateCondition(
            aggregateTypeText = aggregateTypeText,
            aggregateArgs = aggregateArgs
          )
        case _ =>
          RuleConditionText.parse(value).map(RuleCondition.Plain.apply)

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
  ): Either[TaktPrimitiveError, RuleConditionTexts] =
    QuotedConditionRegex
      .findAllMatchIn(value)
      .map(_.group(1))
      .toVector
      .foldLeft[Either[TaktPrimitiveError, Vector[RuleConditionText]]](Right(Vector.empty)) {
        case (acc, currentText) =>
          for
            parsedTexts <- acc
            parsedText <- RuleConditionText.parse(currentText)
          yield parsedTexts :+ parsedText
      }
      .flatMap: parsedTexts =>
        if parsedTexts.isEmpty then Left(TaktPrimitiveError.InvalidRuleConditionSyntax)
        else Right(RuleConditionTexts.create(parsedTexts))
