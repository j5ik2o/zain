package zain.core.takt.primitives

final case class PartInstruction private (value: String)

object PartInstruction:
  def parse(value: String): Either[TaktPrimitiveError, PartInstruction] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyPartInstruction)
    else Right(PartInstruction(value))
