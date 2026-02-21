package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class ArpeggioConcurrency private (value: Int)

object ArpeggioConcurrency:
  def parse(value: Int): Either[PieceDefinitionError, ArpeggioConcurrency] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveArpeggioConcurrency)
    else Right(ArpeggioConcurrency(value))
