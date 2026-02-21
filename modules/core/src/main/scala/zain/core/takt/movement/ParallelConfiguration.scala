package zain.core.takt.movement

final case class ParallelConfiguration private (
    subMovements: MovementDefinitions
)

object ParallelConfiguration:
  val Empty: ParallelConfiguration =
    ParallelConfiguration(MovementDefinitions.Empty)

  def create(subMovements: MovementDefinitions): ParallelConfiguration =
    ParallelConfiguration(subMovements)
