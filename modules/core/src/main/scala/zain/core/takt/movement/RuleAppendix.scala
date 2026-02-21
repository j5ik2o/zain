package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class RuleAppendix private (value: String)

object RuleAppendix:
  def parse(value: String): Either[PieceDefinitionError, RuleAppendix] =
    if value.isEmpty then Left(PieceDefinitionError.EmptyRuleAppendix)
    else Right(RuleAppendix(value))
