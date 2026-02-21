package zain.core.takt.primitives

final case class MovementName private (value: String)

object MovementName:
  def parse(value: String): Either[TaktPrimitiveError, MovementName] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyMovementIdentifier)
    else Right(MovementName(value))
