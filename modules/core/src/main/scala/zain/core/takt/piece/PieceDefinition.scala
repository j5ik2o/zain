package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.primitives.MaxMovements
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final case class PieceDefinition private (
    name: PieceName,
    movements: MovementDefinitions,
    initialMovement: MovementName,
    maxMovements: MaxMovements
):
  def movementNames: Set[MovementName] =
    movements.names

object PieceDefinition:
  private[piece] def create(
      name: PieceName,
      movements: MovementDefinitions,
      initialMovement: MovementName,
      maxMovements: MaxMovements
  ): PieceDefinition =
    PieceDefinition(
      name = name,
      movements = movements,
      initialMovement = initialMovement,
      maxMovements = maxMovements
    )
