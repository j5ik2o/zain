package zain.core.takt.piece

import zain.core.takt.primitives.MovementName

final case class LoopMonitorConfiguration private (
    cycle: Vector[MovementName],
    threshold: Int,
    judge: LoopMonitorJudge
)

object LoopMonitorConfiguration:
  def parse(
      cycle: Vector[MovementName],
      threshold: Int,
      judge: LoopMonitorJudge
  ): Either[PieceDefinitionError, LoopMonitorConfiguration] =
    for
      parsedCycle <- parseCycle(cycle)
      parsedThreshold <- parseThreshold(threshold)
    yield LoopMonitorConfiguration(
      cycle = parsedCycle,
      threshold = parsedThreshold,
      judge = judge
    )

  def create(
      cycle: Vector[MovementName],
      threshold: Int,
      judge: LoopMonitorJudge
  ): Either[PieceDefinitionError, LoopMonitorConfiguration] =
    parse(
      cycle = cycle,
      threshold = threshold,
      judge = judge
    )

  private def parseCycle(
      cycle: Vector[MovementName]
  ): Either[PieceDefinitionError, Vector[MovementName]] =
    if cycle.isEmpty then Left(PieceDefinitionError.EmptyLoopMonitorCycle)
    else Right(cycle)

  private def parseThreshold(value: Int): Either[PieceDefinitionError, Int] =
    if value <= 0 then Left(PieceDefinitionError.NonPositiveLoopMonitorThreshold)
    else Right(value)
