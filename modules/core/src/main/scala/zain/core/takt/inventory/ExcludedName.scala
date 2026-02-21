package zain.core.takt.inventory

final case class ExcludedName private (value: String)

object ExcludedName:
  def parse(value: String): Either[ReferenceModelInventoryError, ExcludedName] =
    if value.isEmpty then Left(ReferenceModelInventoryError.EmptyExcludedName)
    else Right(ExcludedName(value))
