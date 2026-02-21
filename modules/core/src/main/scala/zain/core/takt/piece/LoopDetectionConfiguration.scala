package zain.core.takt.piece

final case class LoopDetectionConfiguration private (
    maxConsecutiveSameStep: Int,
    action: LoopDetectionAction
)

object LoopDetectionConfiguration:
  val Default: LoopDetectionConfiguration = LoopDetectionConfiguration(
    maxConsecutiveSameStep = 10,
    action = LoopDetectionAction.Warn
  )

  def parse(
      maxConsecutiveSameStep: Option[Int],
      action: Option[LoopDetectionAction]
  ): Either[PieceDefinitionError, LoopDetectionConfiguration] =
    parseMaxConsecutiveSameStep(maxConsecutiveSameStep.getOrElse(Default.maxConsecutiveSameStep))
      .map: parsedMaxConsecutiveSameStep =>
        LoopDetectionConfiguration(
          maxConsecutiveSameStep = parsedMaxConsecutiveSameStep,
          action = action.getOrElse(Default.action)
        )

  def create(
      maxConsecutiveSameStep: Option[Int],
      action: Option[LoopDetectionAction]
  ): Either[PieceDefinitionError, LoopDetectionConfiguration] =
    parse(
      maxConsecutiveSameStep = maxConsecutiveSameStep,
      action = action
    )

  private def parseMaxConsecutiveSameStep(value: Int): Either[PieceDefinitionError, Int] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveLoopDetectionMaxConsecutiveSameStep)
    else Right(value)
