package zain.core.takt.piece.evaluation

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.piece.PieceExecutionError

final class RuleEvaluator(
    movement: MovementDefinition,
    context: RuleEvaluatorContext
):
  def evaluate(
      agentContent: String,
      tagContent: String
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    if movement.rules.isEmpty then Right(None)
    else detectMatchByOrder(agentContent, tagContent)

  private def detectMatchByOrder(
      agentContent: String,
      tagContent: String
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    detectAggregateMatch
      .orElse(detectPhase3TagMatch(tagContent))
      .orElse(detectPhase1TagMatch(agentContent)) match
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

  private def detectPhase3TagMatch(content: String): Option[RuleMatch] =
    detectTagMatch(content, RuleMatchMethod.Phase3Tag)

  private def detectPhase1TagMatch(content: String): Option[RuleMatch] =
    detectTagMatch(content, RuleMatchMethod.Phase1Tag)

  private def detectTagMatch(
      content: String,
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
      agentContent: String
  ): Either[PieceExecutionError, Option[RuleMatch]] =
    detectAiConditionMatch(agentContent).flatMap:
      case Some(matchResult) =>
        Right(Some(matchResult))
      case None =>
        detectAiFallbackMatch(agentContent)

  private def detectAiConditionMatch(
      agentContent: String
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
      val judgeConditions = aiConditions.zipWithIndex.map {
        case ((_, text), judgeIndex) =>
          RuleJudgeCondition(
            index = judgeIndex,
            text = text
          )
      }

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
      agentContent: String
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
      val judgeConditions = allConditions.zipWithIndex.map {
        case ((_, text), judgeIndex) =>
          RuleJudgeCondition(
            index = judgeIndex,
            text = text
          )
      }

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
