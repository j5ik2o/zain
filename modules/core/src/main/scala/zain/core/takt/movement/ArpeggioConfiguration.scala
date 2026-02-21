package zain.core.takt.movement

final case class ArpeggioConfiguration private (
    batchSize: ArpeggioBatchSize,
    concurrency: ArpeggioConcurrency
)

object ArpeggioConfiguration:
  val Default: ArpeggioConfiguration = ArpeggioConfiguration(
    batchSize = ArpeggioBatchSize.parse(1).fold(_ => throw new IllegalStateException("invalid default batch size"), identity),
    concurrency = ArpeggioConcurrency.parse(1).fold(_ => throw new IllegalStateException("invalid default concurrency"), identity)
  )

  def parse(
      batchSize: ArpeggioBatchSize,
      concurrency: ArpeggioConcurrency
  ): Either[zain.core.takt.piece.PieceDefinitionError, ArpeggioConfiguration] =
    Right(
      ArpeggioConfiguration(
        batchSize = batchSize,
        concurrency = concurrency
      )
    )

  def create(
      batchSize: ArpeggioBatchSize,
      concurrency: ArpeggioConcurrency
  ): Either[zain.core.takt.piece.PieceDefinitionError, ArpeggioConfiguration] =
    parse(
      batchSize = batchSize,
      concurrency = concurrency
    )
