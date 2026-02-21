package zain.core.takt.piece

final case class LoopDetectionMaxConsecutiveSameStep private (value: Int)

object LoopDetectionMaxConsecutiveSameStep:
  def parse(value: Int): Either[PieceDefinitionError, LoopDetectionMaxConsecutiveSameStep] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveLoopDetectionMaxConsecutiveSameStep)
    else Right(LoopDetectionMaxConsecutiveSameStep(value))
