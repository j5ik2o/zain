package zain.core.takt.primitives

final case class UserInput private (value: String):
  def truncate(maxLength: Int): UserInput =
    UserInput(value.take(maxLength))

object UserInput:
  def parse(value: String): Either[TaktPrimitiveError, UserInput] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyUserInput)
    else Right(UserInput(value))
