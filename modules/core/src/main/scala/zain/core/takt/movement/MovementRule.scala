package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.RuleCondition
import zain.core.takt.primitives.TransitionTarget

final case class MovementRule private (
    condition: RuleCondition,
    next: Option[TransitionTarget]
)

object MovementRule:
  def create(condition: String, next: Option[String]): Either[PieceDefinitionError, MovementRule] =
    for
      parsedCondition <- RuleCondition
        .parse(condition)
        .left
        .map(_ => PieceDefinitionError.EmptyRuleCondition)
      parsedNext <- parseNext(next)
    yield MovementRule(
      condition = parsedCondition,
      next = parsedNext
    )

  private def parseNext(next: Option[String]): Either[PieceDefinitionError, Option[TransitionTarget]] =
    next match
      case None => Right(None)
      case Some(value) =>
        TransitionTarget
          .parse(value)
          .left
          .map(_ => PieceDefinitionError.EmptyRuleTransitionTarget)
          .map(Some(_))
