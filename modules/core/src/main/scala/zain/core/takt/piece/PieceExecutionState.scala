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
    movementOutputs: Map[MovementName, MovementOutput],
    lastOutput: Option[MovementOutput],
    userInputs: Vector[String],
    personaSessions: Map[String, String],
    movementIterations: Map[MovementName, IterationCount],
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

  def startCurrentMovementExecution: Either[PieceExecutionError, PieceExecutionState] =
    status match
      case PieceExecutionStatus.Running =>
        val currentIteration = movementIterations.getOrElse(currentMovement, IterationCount.Zero)
        val nextMovementIteration = currentIteration.increment

        Right(
          copy(
            iteration = iteration.increment,
            movementIterations = movementIterations.updated(currentMovement, nextMovementIteration)
          )
        )
      case terminalStatus =>
        Left(PieceExecutionError.AlreadyFinished(terminalStatus))

  def recordMovementOutput(
      movementName: MovementName,
      movementOutput: MovementOutput
  ): PieceExecutionState =
    copy(
      movementOutputs = movementOutputs.updated(movementName, movementOutput),
      lastOutput = Some(movementOutput)
    )

  def appendUserInput(input: String): PieceExecutionState =
    val truncated = input.take(PieceExecutionState.MaxUserInputLength)
    val nextInputs =
      if userInputs.size >= PieceExecutionState.MaxUserInputs then
        userInputs.tail :+ truncated
      else userInputs :+ truncated

    copy(userInputs = nextInputs)

  def recordPersonaSession(
      persona: String,
      sessionId: String
  ): PieceExecutionState =
    copy(personaSessions = personaSessions.updated(persona, sessionId))

  def incrementMovementIteration(
      movementName: MovementName
  ): PieceExecutionState =
    val currentIteration = movementIterations.getOrElse(movementName, IterationCount.Zero)
    val nextIteration = currentIteration.increment

    copy(movementIterations = movementIterations.updated(movementName, nextIteration))

  def matchedRuleIndexOf(movementName: MovementName): Option[Int] =
    movementOutputs.get(movementName).flatMap(_.matchedRuleIndex)

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
              Right(copy(currentMovement = nextMovement))
            else Left(PieceExecutionError.UndefinedTransitionMovement(nextMovement))
      case terminalStatus =>
        Left(PieceExecutionError.AlreadyFinished(terminalStatus))

  def transitionByMatchedRuleIndex(
      movementDefinition: zain.core.takt.movement.MovementDefinition,
      matchedRuleIndex: Int
  ): Either[PieceExecutionError, PieceExecutionState] =
    if movementDefinition.name != currentMovement then
      Left(
        PieceExecutionError.MovementDefinitionMismatch(
          currentMovement = currentMovement,
          providedMovement = movementDefinition.name
        )
      )
    else
      movementDefinition
        .transitionTargetByMatchedRuleIndex(matchedRuleIndex)
        .flatMap(target =>
          startCurrentMovementExecution.flatMap(_.transitionTo(target))
        )

object PieceExecutionState:
  private val MaxUserInputs = 100
  private val MaxUserInputLength = 10000

  def start(pieceDefinition: PieceDefinition): PieceExecutionState =
    PieceExecutionState(
      pieceName = pieceDefinition.name,
      allowedMovements = pieceDefinition.movementNames,
      currentMovement = pieceDefinition.initialMovement,
      iteration = IterationCount.Zero,
      movementOutputs = Map.empty,
      lastOutput = None,
      userInputs = Vector.empty,
      personaSessions = Map.empty,
      movementIterations = Map.empty,
      status = PieceExecutionStatus.Running
    )
