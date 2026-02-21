package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final case class PieceDraft(
    name: PieceName,
    movements: Vector[MovementDefinition],
    initialMovement: Option[MovementName],
    maxMovements: Option[Int]
)
