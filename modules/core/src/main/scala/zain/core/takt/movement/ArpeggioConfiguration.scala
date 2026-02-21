package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

final case class ArpeggioConfiguration private (
    batchSize: Int,
    concurrency: Int
)

object ArpeggioConfiguration:
  val Default: ArpeggioConfiguration = ArpeggioConfiguration(
    batchSize = 1,
    concurrency = 1
  )

  def parse(
      batchSize: Int,
      concurrency: Int
  ): Either[PieceDefinitionError, ArpeggioConfiguration] =
    for
      parsedBatchSize <- parseBatchSize(batchSize)
      parsedConcurrency <- parseConcurrency(concurrency)
    yield ArpeggioConfiguration(
      batchSize = parsedBatchSize,
      concurrency = parsedConcurrency
    )

  def create(
      batchSize: Int,
      concurrency: Int
  ): Either[PieceDefinitionError, ArpeggioConfiguration] =
    parse(
      batchSize = batchSize,
      concurrency = concurrency
    )

  private def parseBatchSize(value: Int): Either[PieceDefinitionError, Int] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveArpeggioBatchSize)
    else Right(value)

  private def parseConcurrency(value: Int): Either[PieceDefinitionError, Int] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveArpeggioConcurrency)
    else Right(value)
