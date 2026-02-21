package zain.core.takt.piece

import zain.core.takt.primitives.IterationCount
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName
import zain.core.takt.primitives.TransitionTarget

final case class PieceExecutionState private (
    pieceName: PieceName,
    allowedMovements: Set[MovementName],
    currentMovement: MovementName,
    iteration: IterationCount,
    status: PieceExecutionStatus
):
  def markCompleted: Either[PieceExecutionError, PieceExecutionState] =
    status match
      case PieceExecutionStatus.Running =>
        Right(copy(status = PieceExecutionStatus.Completed))
      case terminalStatus =>
        Left(PieceExecutionError.AlreadyFinished(terminalStatus))

  def markAborted: Either[PieceExecutionError, PieceExecutionState] =
    status match
      case PieceExecutionStatus.Running =>
        Right(copy(status = PieceExecutionStatus.Aborted))
      case terminalStatus =>
        Left(PieceExecutionError.AlreadyFinished(terminalStatus))

  def withIteration(nextIteration: IterationCount): PieceExecutionState =
    copy(iteration = nextIteration)

  def transitionTo(target: TransitionTarget): Either[PieceExecutionError, PieceExecutionState] =
    status match
      case PieceExecutionStatus.Running =>
        target match
          case TransitionTarget.Complete =>
            markCompleted
          case TransitionTarget.Abort =>
            markAborted
          case TransitionTarget.Movement(nextMovement) =>
            if allowedMovements.contains(nextMovement) then
              Right(
                copy(currentMovement = nextMovement)
                  .withIteration(iteration.increment)
              )
            else Left(PieceExecutionError.UndefinedTransitionMovement(nextMovement))
      case terminalStatus =>
        Left(PieceExecutionError.AlreadyFinished(terminalStatus))

object PieceExecutionState:
  def start(pieceDefinition: PieceDefinition): PieceExecutionState =
    PieceExecutionState(
      pieceName = pieceDefinition.name,
      allowedMovements = pieceDefinition.movementNames,
      currentMovement = pieceDefinition.initialMovement,
      iteration = IterationCount.Zero,
      status = PieceExecutionStatus.Running
    )
