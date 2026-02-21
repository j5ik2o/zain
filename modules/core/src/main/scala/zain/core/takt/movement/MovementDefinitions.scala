package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.MovementName

final case class MovementDefinitions private (
    private val values: Vector[MovementDefinition]
):
  def isEmpty: Boolean =
    values.isEmpty

  def headOption: Option[MovementDefinition] =
    values.headOption

  def names: Set[MovementName] =
    values.map(_.name).toSet

  def parseNonEmpty: Either[PieceDefinitionError, MovementDefinitions] =
    if isEmpty then Left(PieceDefinitionError.EmptyMovements)
    else Right(this)

  def parseUniqueNames: Either[PieceDefinitionError, MovementDefinitions] =
    foldLeft[Either[PieceDefinitionError, Set[MovementName]]](Right(Set.empty)) { (acc, movement) =>
      acc.flatMap: seen =>
        if seen.contains(movement.name) then Left(PieceDefinitionError.DuplicateMovementName(movement.name))
        else Right(seen + movement.name)
    }.map(_ => this)

  def parseTransitionTargets: Either[PieceDefinitionError, MovementDefinitions] =
    val movementNames = names

    foldLeft[Either[PieceDefinitionError, MovementDefinitions]](Right(MovementDefinitions.Empty)) {
      (acc, movement) =>
        for
          parsedMovements <- acc
          parsedMovement <- movement.parseTransitionTargets(movementNames)
        yield parsedMovements :+ parsedMovement
    }

  def parseInitialMovement(
      initialMovement: Option[MovementName]
  ): Either[PieceDefinitionError, MovementName] =
    initialMovement match
      case None =>
        headOption
          .map(_.name)
          .toRight(PieceDefinitionError.EmptyMovements)
      case Some(value) =>
        if names.contains(value) then Right(value)
        else Left(PieceDefinitionError.InitialMovementNotFound(value))

  def foldLeft[B](initial: B)(operation: (B, MovementDefinition) => B): B =
    values.foldLeft(initial)(operation)

  def :+(movement: MovementDefinition): MovementDefinitions =
    MovementDefinitions(values :+ movement)

  def breachEncapsulationOfValues: Vector[MovementDefinition] =
    values

object MovementDefinitions:
  val Empty: MovementDefinitions = MovementDefinitions(Vector.empty)

  def create(values: Vector[MovementDefinition]): MovementDefinitions =
    MovementDefinitions(values)
