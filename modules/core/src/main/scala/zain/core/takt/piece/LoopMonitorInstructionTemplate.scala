package zain.core.takt.piece

final case class LoopMonitorInstructionTemplate private (value: String)

object LoopMonitorInstructionTemplate:
  def parse(value: String): Either[PieceDefinitionError, LoopMonitorInstructionTemplate] =
    Right(LoopMonitorInstructionTemplate(value))
