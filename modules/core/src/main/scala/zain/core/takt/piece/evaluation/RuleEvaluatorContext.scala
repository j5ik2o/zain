package zain.core.takt.piece.evaluation

import zain.core.takt.piece.PieceExecutionState

final case class RuleEvaluatorContext(
    state: PieceExecutionState,
    interactive: Boolean,
    detectRuleIndex: RuleIndexDetector,
    aiConditionJudge: AiConditionJudge
)
