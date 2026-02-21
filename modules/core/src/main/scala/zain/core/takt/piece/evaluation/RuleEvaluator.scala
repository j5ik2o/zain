package zain.core.takt.piece.evaluation

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.piece.PieceExecutionError
import zain.core.takt.primitives.AgentOutput
import zain.core.takt.primitives.RuleDetectionContent

final class RuleEvaluator(
    movement: MovementDefinition,
    context: RuleEvaluatorContext
):
  def evaluate(
      agentContent: AgentOutput,
      tagContent: RuleDetectionContent
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    if movement.rules.isEmpty then Right(None)
    else detectMatchByOrder(agentContent, tagContent)

  private def detectMatchByOrder(
      agentContent: AgentOutput,
      tagContent: RuleDetectionContent
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    detectAggregateMatch
      .orElse(detectPhase3TagMatch(tagContent))
      .orElse(detectPhase1TagMatch(RuleDetectionContent.from(agentContent.value))) match
      case Some(matchResult) =>
        Right(Some(matchResult))
      case None =>
        detectAiMatches(agentContent).flatMap:
          case Some(matchResult) =>
            Right(Some(matchResult))
          case None =>
            Left(PieceExecutionError.RuleNotMatched(movement.name))

  private def detectAggregateMatch: Option[RuleMatch] =
    val aggregateEvaluator = new AggregateEvaluator(movement, context.state)
    aggregateEvaluator.evaluate().map: index =>
      RuleMatch(
        index = index,
        method = RuleMatchMethod.Aggregate
      )

  private def detectPhase3TagMatch(content: RuleDetectionContent): Option[RuleMatch] =
    detectTagMatch(content, RuleMatchMethod.Phase3Tag)

  private def detectPhase1TagMatch(content: RuleDetectionContent): Option[RuleMatch] =
    detectTagMatch(content, RuleMatchMethod.Phase1Tag)

  private def detectTagMatch(
      content: RuleDetectionContent,
      method: RuleMatchMethod
  ): Option[RuleMatch] =
    if content.isEmpty then None
    else
      context.detectRuleIndex
        .detect(content, movement.name)
        .flatMap(ruleIndex =>
          if isSelectableRule(ruleIndex) then
            Some(
              RuleMatch(
                index = ruleIndex,
                method = method
              )
            )
          else None
        )

  private def detectAiMatches(
      agentContent: AgentOutput
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    detectAiConditionMatch(agentContent).flatMap:
      case Some(matchResult) =>
        Right(Some(matchResult))
      case None =>
        detectAiFallbackMatch(agentContent)

  private def detectAiConditionMatch(
      agentContent: AgentOutput
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    val aiConditions = movement.rules.indices
      .flatMap(index =>
        movement.rules.ruleAt(index).flatMap: rule =>
          if !isSelectableRule(index) then None
          else
            rule.condition.aiConditionText.map(text => (index, text))
      )
      .toVector

    if aiConditions.isEmpty then Right(None)
    else
      parseJudgeConditions(aiConditions).flatMap: judgeConditions =>
        context.aiConditionJudge
          .judge(agentContent, judgeConditions)
          .flatMap:
            case None =>
              Right(None)
            case Some(judgeIndex) =>
              if judgeIndex >= 0 && judgeIndex < aiConditions.size then
                val (ruleIndex, _) = aiConditions(judgeIndex)
                Right(
                  Some(
                    RuleMatch(
                      index = ruleIndex,
                      method = RuleMatchMethod.AiJudge
                    )
                  )
                )
              else
                Right(None)

  private def detectAiFallbackMatch(
      agentContent: AgentOutput
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    val allConditions = movement.rules.indices
      .flatMap(index =>
        movement.rules.ruleAt(index).flatMap: rule =>
          if !isSelectableRule(index) then None
          else Some((index, rule.condition.breachEncapsulationOfRawValue))
      )
      .toVector

    if allConditions.isEmpty then Right(None)
    else
      parseJudgeConditions(allConditions).flatMap: judgeConditions =>
        context.aiConditionJudge
          .judge(agentContent, judgeConditions)
          .flatMap:
            case None =>
              Right(None)
            case Some(judgeIndex) =>
              if judgeIndex >= 0 && judgeIndex < allConditions.size then
                val (ruleIndex, _) = allConditions(judgeIndex)
                Right(
                  Some(
                    RuleMatch(
                      index = ruleIndex,
                      method = RuleMatchMethod.AiJudgeFallback
                    )
                  )
                )
              else Right(None)

  private def isSelectableRule(ruleIndex: Int): Boolean =
    movement.rules.ruleAt(ruleIndex).exists(rule => context.interactive || !rule.interactiveOnly)

  private def parseJudgeConditions(
      indexedTexts: Vector[(Int, String)]
  ): Either[PieceExecutionError, RuleJudgeConditions] =
    indexedTexts
      .zipWithIndex
      .foldLeft[Either[PieceExecutionError, Vector[RuleJudgeCondition]]](Right(Vector.empty)) {
        case (acc, ((_, text), judgeIndex)) =>
          for
            parsedConditions <- acc
            parsedCondition <- RuleJudgeCondition.parse(
              index = judgeIndex,
              text = text
            )
          yield parsedConditions :+ parsedCondition
      }
      .map(RuleJudgeConditions.create)
