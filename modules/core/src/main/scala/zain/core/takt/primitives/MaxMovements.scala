package zain.core.takt.primitives

final case class MaxMovements private (value: Int)

object MaxMovements:
  def parse(value: Int): Either[TaktPrimitiveError, MaxMovements] =
    if value <= 0 then Left(TaktPrimitiveError.NonPositiveMaxMovements)
    else Right(MaxMovements(value))
