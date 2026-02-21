package zain.core.takt.piece.evaluation

import zain.core.takt.piece.PieceExecutionError

final case class RuleJudgeConditionIndex private (value: Int)

object RuleJudgeConditionIndex:
  def parse(value: Int): Either[PieceExecutionError, RuleJudgeConditionIndex] =
    if value < 0 then Left(PieceExecutionError.NegativeRuleJudgeConditionIndex(value))
    else Right(RuleJudgeConditionIndex(value))
