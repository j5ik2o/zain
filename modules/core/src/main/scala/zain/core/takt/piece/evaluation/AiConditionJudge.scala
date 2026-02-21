package zain.core.takt.piece.evaluation

import zain.core.takt.piece.PieceExecutionError
import zain.core.takt.primitives.AgentOutput

trait AiConditionJudge:
  def judge(
      agentOutput: AgentOutput,
      conditions: RuleJudgeConditions
  ): Either[PieceExecutionError, Option[Int]]
