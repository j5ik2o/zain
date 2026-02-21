package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class ArpeggioBatchSize private (value: Int)

object ArpeggioBatchSize:
  def parse(value: Int): Either[PieceDefinitionError, ArpeggioBatchSize] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveArpeggioBatchSize)
    else Right(ArpeggioBatchSize(value))
