package zain.core.takt.primitives

final case class PartTitle private (value: String)

object PartTitle:
  def parse(value: String): Either[TaktPrimitiveError, PartTitle] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyPartTitle)
    else Right(PartTitle(value))
