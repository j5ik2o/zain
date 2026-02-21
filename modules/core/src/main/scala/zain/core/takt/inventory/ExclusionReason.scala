package zain.core.takt.inventory

final case class ExclusionReason private (value: String)

object ExclusionReason:
  def create(
      excludedName: ExcludedName,
      value: String
  ): Either[ReferenceModelInventoryError, ExclusionReason] =
    if value.isEmpty then Left(ReferenceModelInventoryError.MissingExclusionReason(excludedName))
    else Right(ExclusionReason(value))
