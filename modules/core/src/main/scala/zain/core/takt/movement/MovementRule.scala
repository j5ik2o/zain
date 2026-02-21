package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.RuleCondition
import zain.core.takt.primitives.TransitionTarget

final case class MovementRule private (
    condition: RuleCondition,
    next: Option[TransitionTarget],
    appendix: Option[RuleAppendix],
    requiresUserInput: Boolean,
    interactiveOnly: Boolean
)

object MovementRule:
  def create(condition: String, next: Option[String]): Either[PieceDefinitionError, MovementRule] =
    parse(
      condition = condition,
      next = next,
      appendix = None,
      requiresUserInput = false,
      interactiveOnly = false
    )

  def parse(condition: String, next: Option[String]): Either[PieceDefinitionError, MovementRule] =
    parse(
      condition = condition,
      next = next,
      appendix = None,
      requiresUserInput = false,
      interactiveOnly = false
    )

  def create(
      condition: String,
      next: Option[String],
      appendix: Option[RuleAppendix],
      requiresUserInput: Boolean,
      interactiveOnly: Boolean
  ): Either[PieceDefinitionError, MovementRule] =
    parse(
      condition = condition,
      next = next,
      appendix = appendix,
      requiresUserInput = requiresUserInput,
      interactiveOnly = interactiveOnly
    )

  def parse(
      condition: String,
      next: Option[String],
      appendix: Option[RuleAppendix],
      requiresUserInput: Boolean,
      interactiveOnly: Boolean
  ): Either[PieceDefinitionError, MovementRule] =
    for
      parsedCondition <- parseCondition(condition)
      parsedNext <- parseNext(next)
    yield MovementRule(
      condition = parsedCondition,
      next = parsedNext,
      appendix = appendix,
      requiresUserInput = requiresUserInput,
      interactiveOnly = interactiveOnly
    )

  private def parseCondition(condition: String): Either[PieceDefinitionError, RuleCondition] =
    RuleCondition
      .parse(condition)
      .left
      .map:
        case zain.core.takt.primitives.TaktPrimitiveError.EmptyRuleCondition =>
          PieceDefinitionError.EmptyRuleCondition
        case _ =>
          PieceDefinitionError.InvalidRuleCondition

  private def parseNext(next: Option[String]): Either[PieceDefinitionError, Option[TransitionTarget]] =
    next match
      case None => Right(None)
      case Some(value) =>
        TransitionTarget
          .parse(value)
          .left
          .map(_ => PieceDefinitionError.EmptyRuleTransitionTarget)
          .map(Some(_))
