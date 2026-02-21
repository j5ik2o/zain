package zain.core.takt.piece

final case class LoopMonitorThreshold private (value: Int)

object LoopMonitorThreshold:
  def parse(value: Int): Either[PieceDefinitionError, LoopMonitorThreshold] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveLoopMonitorThreshold)
    else Right(LoopMonitorThreshold(value))
