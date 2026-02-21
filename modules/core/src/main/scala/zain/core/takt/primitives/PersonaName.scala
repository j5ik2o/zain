package zain.core.takt.primitives

final case class PersonaName private (value: String)

object PersonaName:
  def parse(value: String): Either[TaktPrimitiveError, PersonaName] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyPersonaName)
    else Right(PersonaName(value))
