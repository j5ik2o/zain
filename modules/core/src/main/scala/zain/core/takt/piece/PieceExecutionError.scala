package zain.core.takt.piece

import zain.core.takt.primitives.MovementName

enum PieceExecutionError:
  case UndefinedTransitionMovement(target: MovementName)
  case AlreadyFinished(currentStatus: PieceExecutionStatus)
