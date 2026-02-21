package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.primitives.MaxMovements
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final case class PieceDefinition private (
    name: PieceName,
    movements: Vector[MovementDefinition],
    initialMovement: MovementName,
    maxMovements: MaxMovements
):
  def movementNames: Set[MovementName] =
    movements.map(_.name).toSet

object PieceDefinition:
  private[piece] def create(
      name: PieceName,
      movements: Vector[MovementDefinition],
      initialMovement: MovementName,
      maxMovements: MaxMovements
  ): PieceDefinition =
    PieceDefinition(
      name = name,
      movements = movements,
      initialMovement = initialMovement,
      maxMovements = maxMovements
    )
