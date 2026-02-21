package zain.core.takt.primitives

final case class PartTimeoutMillis private (value: Int)

object PartTimeoutMillis:
  def parse(value: Int): Either[TaktPrimitiveError, PartTimeoutMillis] =
    if value <= 0 then Left(TaktPrimitiveError.NonPositivePartTimeoutMillis)
    else Right(PartTimeoutMillis(value))
