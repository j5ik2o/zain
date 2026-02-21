package zain.core.takt.primitives

final case class PersonaSessionId private (value: String)

object PersonaSessionId:
  def parse(value: String): Either[TaktPrimitiveError, PersonaSessionId] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyPersonaSessionId)
    else Right(PersonaSessionId(value))
