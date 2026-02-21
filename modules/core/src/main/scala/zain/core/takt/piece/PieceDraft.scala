package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final case class PieceDraft(
    name: PieceName,
    movements: MovementDefinitions,
    initialMovement: Option[MovementName],
    maxMovements: Option[Int],
    loopDetection: Option[LoopDetectionConfiguration] = None,
    loopMonitors: LoopMonitorConfigurations = LoopMonitorConfigurations.Empty
)
