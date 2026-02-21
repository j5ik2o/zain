package zain.core.takt.primitives

final case class RuleCondition private (value: String)

object RuleCondition:
  def parse(value: String): Either[TaktPrimitiveError, RuleCondition] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyRuleCondition)
    else Right(RuleCondition(value))
