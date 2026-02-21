package zain.core.takt.primitives

final case class FacetName private (value: String)

object FacetName:
  def parse(value: String): Either[TaktPrimitiveError, FacetName] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyFacetName)
    else Right(FacetName(value))
