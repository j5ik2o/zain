package zain.core.takt.primitives

final case class RuleConditionText private (value: String)

object RuleConditionText:
  def parse(value: String): Either[TaktPrimitiveError, RuleConditionText] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyRuleCondition)
    else Right(RuleConditionText(value))
