package zain.core.takt.inventory

final case class ReferenceSourcePath private (value: String)

object ReferenceSourcePath:
  def create(
      target: ReferenceModelTarget,
      value: String
  ): Either[ReferenceModelInventoryError, ReferenceSourcePath] =
    if value.isEmpty then Left(ReferenceModelInventoryError.EmptyReferenceSourcePath(target))
    else Right(ReferenceSourcePath(value))
