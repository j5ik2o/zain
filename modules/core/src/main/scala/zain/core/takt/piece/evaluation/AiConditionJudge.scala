package zain.core.takt.piece.evaluation

import zain.core.takt.piece.PieceExecutionError

trait AiConditionJudge:
  def judge(
      agentOutput: String,
      conditions: Vector[RuleJudgeCondition]
  ): Either[PieceExecutionError, Option[Int]]
