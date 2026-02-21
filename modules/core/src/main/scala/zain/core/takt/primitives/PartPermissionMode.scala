package zain.core.takt.primitives

enum PartPermissionMode(val breachEncapsulationOfValue: String):
  case ReadOnly extends PartPermissionMode("readonly")
  case Edit extends PartPermissionMode("edit")
  case Full extends PartPermissionMode("full")

object PartPermissionMode:
  def parse(value: String): Either[TaktPrimitiveError, PartPermissionMode] =
    value match
      case PartPermissionMode.ReadOnly.breachEncapsulationOfValue =>
        Right(PartPermissionMode.ReadOnly)
      case PartPermissionMode.Edit.breachEncapsulationOfValue =>
        Right(PartPermissionMode.Edit)
      case PartPermissionMode.Full.breachEncapsulationOfValue =>
        Right(PartPermissionMode.Full)
      case _ =>
        Left(TaktPrimitiveError.InvalidPartPermissionMode(value))
