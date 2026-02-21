package zain.core.takt.primitives

final case class PartId private (value: String)

object PartId:
  def parse(value: String): Either[TaktPrimitiveError, PartId] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyPartId)
    else Right(PartId(value))
