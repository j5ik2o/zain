package zain.core.takt.piece

import zain.core.takt.primitives.RuleCondition
import zain.core.takt.primitives.TaktPrimitiveError
import zain.core.takt.primitives.TransitionTarget

final case class LoopMonitorRule private (
    condition: RuleCondition,
    next: TransitionTarget
)

object LoopMonitorRule:
  def parse(
      condition: String,
      next: String
  ): Either[PieceDefinitionError, LoopMonitorRule] =
    for
      parsedCondition <- parseCondition(condition)
      parsedNext <- parseNext(next)
    yield LoopMonitorRule(
      condition = parsedCondition,
      next = parsedNext
    )

  def create(
      condition: String,
      next: String
  ): Either[PieceDefinitionError, LoopMonitorRule] =
    parse(
      condition = condition,
      next = next
    )

  private def parseCondition(
      condition: String
  ): Either[PieceDefinitionError, RuleCondition] =
    RuleCondition
      .parse(condition)
      .left
      .map:
        case TaktPrimitiveError.EmptyRuleCondition =>
          PieceDefinitionError.EmptyLoopMonitorRuleCondition
        case _ =>
          PieceDefinitionError.InvalidRuleCondition

  private def parseNext(next: String): Either[PieceDefinitionError, TransitionTarget] =
    TransitionTarget
      .parse(next)
      .left
      .map(_ => PieceDefinitionError.EmptyLoopMonitorRuleTransitionTarget)
