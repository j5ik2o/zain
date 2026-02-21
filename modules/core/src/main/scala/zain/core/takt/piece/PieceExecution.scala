package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.piece.evaluation.AiConditionJudge
import zain.core.takt.piece.evaluation.RuleEvaluator
import zain.core.takt.piece.evaluation.RuleEvaluatorContext
import zain.core.takt.piece.evaluation.RuleIndexDetector
import zain.core.takt.piece.evaluation.RuleMatch
import zain.core.takt.primitives.AgentOutput
import zain.core.takt.primitives.RuleDetectionContent

final case class PieceExecution private (
    definition: PieceDefinition,
    state: PieceExecutionState
):
  def currentMovementDefinition: Either[PieceExecutionError, MovementDefinition] =
    definition
      .movementByName(state.currentMovement)
      .toRight(PieceExecutionError.CurrentMovementNotFound(state.currentMovement))

  def evaluateAndAdvance(
      agentContent: AgentOutput,
      tagContent: RuleDetectionContent,
      interactive: Boolean,
      detectRuleIndex: RuleIndexDetector,
      aiConditionJudge: AiConditionJudge
  ): Either[PieceExecutionError, PieceExecution] =
    for
      movement <- currentMovementDefinition
      matched <- evaluateRuleMatch(
        movement = movement,
        agentContent = agentContent,
        tagContent = tagContent,
        interactive = interactive,
        detectRuleIndex = detectRuleIndex,
        aiConditionJudge = aiConditionJudge
      )
      movementOutput <- MovementOutput.create(
        content = agentContent.value,
        matchedRuleIndex = Some(matched.index)
      )
      transitioned <- state
        .recordMovementOutput(movement.name, movementOutput)
        .transitionByMatchedRuleIndex(movement, matched.index)
    yield copy(state = transitioned)

  private def evaluateRuleMatch(
      movement: MovementDefinition,
      agentContent: AgentOutput,
      tagContent: RuleDetectionContent,
      interactive: Boolean,
      detectRuleIndex: RuleIndexDetector,
      aiConditionJudge: AiConditionJudge
  ): Either[PieceExecutionError, RuleMatch] =
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = state,
        interactive = interactive,
        detectRuleIndex = detectRuleIndex,
        aiConditionJudge = aiConditionJudge
      )
    )

    evaluator
      .evaluate(
        agentContent = agentContent,
        tagContent = tagContent
      )
      .flatMap:
        case Some(result) => Right(result)
        case None         => Left(PieceExecutionError.RuleNotMatched(movement.name))

object PieceExecution:
  def start(definition: PieceDefinition): PieceExecution =
    PieceExecution(
      definition = definition,
      state = PieceExecutionState.start(definition)
    )
