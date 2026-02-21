package zain.core.takt.primitives

final case class IterationCount private (value: Int):
  def increment: IterationCount =
    IterationCount(value + 1)

object IterationCount:
  val Zero: IterationCount = IterationCount(0)

  def parse(value: Int): Either[TaktPrimitiveError, IterationCount] =
    if value < 0 then Left(TaktPrimitiveError.NegativeIterationCount)
    else Right(IterationCount(value))
