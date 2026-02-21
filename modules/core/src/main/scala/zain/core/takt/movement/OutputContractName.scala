package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class OutputContractName private (value: String)

object OutputContractName:
  def parse(value: String): Either[PieceDefinitionError, OutputContractName] =
    if value.isEmpty then Left(PieceDefinitionError.EmptyOutputContractName)
    else Right(OutputContractName(value))
