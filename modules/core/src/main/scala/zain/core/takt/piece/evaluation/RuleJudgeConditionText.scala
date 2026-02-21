package zain.core.takt.piece.evaluation

import zain.core.takt.piece.PieceExecutionError

final case class RuleJudgeConditionText private (value: String)

object RuleJudgeConditionText:
  def parse(value: String): Either[PieceExecutionError, RuleJudgeConditionText] =
    if value.isEmpty then Left(PieceExecutionError.EmptyRuleJudgeConditionText)
    else Right(RuleJudgeConditionText(value))
