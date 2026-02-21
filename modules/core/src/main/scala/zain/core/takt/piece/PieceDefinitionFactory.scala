package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.movement.MovementRule
import zain.core.takt.primitives.MaxMovements
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.TransitionTarget

object PieceDefinitionFactory:
  private val DefaultMaxMovements = 10

  def create(draft: PieceDraft): Either[PieceDefinitionError, PieceDefinition] =
    for
      nonEmptyMovements <- requireMovements(draft.movements)
      _ <- requireUniqueMovementNames(nonEmptyMovements)
      parsedMovements <- parseTransitionTargets(nonEmptyMovements)
      resolvedInitial <- resolveInitialMovement(parsedMovements, draft.initialMovement)
      resolvedMaxMovements <- resolveMaxMovements(draft.maxMovements)
    yield PieceDefinition.create(
      name = draft.name,
      movements = parsedMovements,
      initialMovement = resolvedInitial,
      maxMovements = resolvedMaxMovements
    )

  private def requireMovements(
      movements: Vector[MovementDefinition]
  ): Either[PieceDefinitionError, Vector[MovementDefinition]] =
    if movements.isEmpty then Left(PieceDefinitionError.EmptyMovements)
    else Right(movements)

  private def requireUniqueMovementNames(
      movements: Vector[MovementDefinition]
  ): Either[PieceDefinitionError, Unit] =
    movements.foldLeft[Either[PieceDefinitionError, Set[MovementName]]](Right(Set.empty)) {
      (acc, movement) =>
        acc.flatMap: seen =>
          if seen.contains(movement.name) then Left(PieceDefinitionError.DuplicateMovementName(movement.name))
          else Right(seen + movement.name)
    }.map(_ => ())

  private def resolveInitialMovement(
      movements: Vector[MovementDefinition],
      initialMovement: Option[MovementName]
  ): Either[PieceDefinitionError, MovementName] =
    val movementNames = movements.map(_.name)

    initialMovement match
      case None => Right(movementNames.head)
      case Some(value) =>
        if movementNames.contains(value) then Right(value)
        else Left(PieceDefinitionError.InitialMovementNotFound(value))

  private def parseTransitionTargets(
      movements: Vector[MovementDefinition]
  ): Either[PieceDefinitionError, Vector[MovementDefinition]] =
    val movementNames = movements.map(_.name).toSet

    movements.foldLeft[Either[PieceDefinitionError, Vector[MovementDefinition]]](Right(Vector.empty)) {
      (acc, movement) =>
        for
          parsedMovements <- acc
          parsedMovement <- parseMovementTransitions(movement, movementNames)
        yield parsedMovements :+ parsedMovement
    }

  private def parseMovementTransitions(
      movement: MovementDefinition,
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, MovementDefinition] =
    movement.rules.foldLeft[Either[PieceDefinitionError, Vector[MovementRule]]](Right(Vector.empty)) {
      (acc, rule) =>
        for
          parsedRules <- acc
          parsedRule <- parseRuleTransitionTarget(rule, movement, movementNames)
        yield parsedRules :+ parsedRule
    }.map(_ => movement)

  private def parseRuleTransitionTarget(
      rule: MovementRule,
      movement: MovementDefinition,
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, MovementRule] =
    rule.next match
      case Some(TransitionTarget.Movement(target)) =>
        if movementNames.contains(target) then Right(rule)
        else Left(PieceDefinitionError.UndefinedTransitionTarget(target = target, from = movement.name))
      case _ =>
        Right(rule)

  private def resolveMaxMovements(maxMovements: Option[Int]): Either[PieceDefinitionError, MaxMovements] =
    MaxMovements
      .parse(maxMovements.getOrElse(DefaultMaxMovements))
      .left
      .map(_ => PieceDefinitionError.NonPositiveMaxMovements)
