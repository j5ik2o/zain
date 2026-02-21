package zain.core.takt.piece

import zain.core.takt.primitives.MovementNames

final case class LoopMonitorConfiguration private (
    cycle: MovementNames,
    threshold: LoopMonitorThreshold,
    judge: LoopMonitorJudge
)

object LoopMonitorConfiguration:
  def parse(
      cycle: MovementNames,
      threshold: LoopMonitorThreshold,
      judge: LoopMonitorJudge
  ): Either[PieceDefinitionError, LoopMonitorConfiguration] =
    for
      parsedCycle <- parseCycle(cycle)
    yield LoopMonitorConfiguration(
      cycle = parsedCycle,
      threshold = threshold,
      judge = judge
    )

  def create(
      cycle: MovementNames,
      threshold: LoopMonitorThreshold,
      judge: LoopMonitorJudge
  ): Either[PieceDefinitionError, LoopMonitorConfiguration] =
    parse(
      cycle = cycle,
      threshold = threshold,
      judge = judge
    )

  private def parseCycle(
      cycle: MovementNames
  ): Either[PieceDefinitionError, MovementNames] =
    if cycle.isEmpty then Left(PieceDefinitionError.EmptyLoopMonitorCycle)
    else if cycle.size < 2 then Left(PieceDefinitionError.LoopMonitorCycleRequiresAtLeastTwoMovements)
    else Right(cycle)
