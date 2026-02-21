package zain.core.takt.primitives

final case class PieceName private (value: String)

object PieceName:
  def parse(value: String): Either[TaktPrimitiveError, PieceName] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyPieceName)
    else Right(PieceName(value))
