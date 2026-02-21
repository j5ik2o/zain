package zain.core.takt.piece

final case class LoopDetectionConfiguration private (
    maxConsecutiveSameStep: LoopDetectionMaxConsecutiveSameStep,
    action: LoopDetectionAction
)

object LoopDetectionConfiguration:
  val Default: LoopDetectionConfiguration = LoopDetectionConfiguration(
    maxConsecutiveSameStep = LoopDetectionMaxConsecutiveSameStep
      .parse(10)
      .fold(_ => throw new IllegalStateException("invalid default loop detection threshold"), identity),
    action = LoopDetectionAction.Warn
  )

  def parse(
      maxConsecutiveSameStep: Option[LoopDetectionMaxConsecutiveSameStep],
      action: Option[LoopDetectionAction]
  ): Either[PieceDefinitionError, LoopDetectionConfiguration] =
    Right(
      LoopDetectionConfiguration(
        maxConsecutiveSameStep = maxConsecutiveSameStep.getOrElse(Default.maxConsecutiveSameStep),
        action = action.getOrElse(Default.action)
      )
    )

  def create(
      maxConsecutiveSameStep: Option[LoopDetectionMaxConsecutiveSameStep],
      action: Option[LoopDetectionAction]
  ): Either[PieceDefinitionError, LoopDetectionConfiguration] =
    parse(
      maxConsecutiveSameStep = maxConsecutiveSameStep,
      action = action
    )
