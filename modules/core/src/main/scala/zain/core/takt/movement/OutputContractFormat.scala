package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class OutputContractFormat private (value: String)

object OutputContractFormat:
  def parse(value: String): Either[PieceDefinitionError, OutputContractFormat] =
    if value.isEmpty then Left(PieceDefinitionError.EmptyOutputContractFormat)
    else Right(OutputContractFormat(value))
