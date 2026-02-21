package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class OutputContractOrder private (value: String)

object OutputContractOrder:
  def parse(value: String): Either[PieceDefinitionError, OutputContractOrder] =
    if value.isEmpty then Left(PieceDefinitionError.EmptyOutputContractOrder)
    else Right(OutputContractOrder(value))
