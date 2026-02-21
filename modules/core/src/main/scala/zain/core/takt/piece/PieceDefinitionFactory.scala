package zain.core.takt.piece

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.movement.MovementRule
import zain.core.takt.movement.MovementRules
import zain.core.takt.primitives.MaxMovements
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.TransitionTarget

object PieceDefinitionFactory:
  private val DefaultMaxMovements = 10

  def create(draft: PieceDraft): Either[PieceDefinitionError, PieceDefinition] =
    for
      nonEmptyMovements <- requireMovements(draft.movements)
      uniqueMovements <- parseUniqueMovementNames(nonEmptyMovements)
      parsedMovements <- parseTransitionTargets(uniqueMovements)
      resolvedInitial <- resolveInitialMovement(parsedMovements, draft.initialMovement)
      resolvedMaxMovements <- resolveMaxMovements(draft.maxMovements)
    yield PieceDefinition.create(
      name = draft.name,
      movements = parsedMovements,
      initialMovement = resolvedInitial,
      maxMovements = resolvedMaxMovements
    )

  private def requireMovements(
      movements: MovementDefinitions
  ): Either[PieceDefinitionError, MovementDefinitions] =
    if movements.isEmpty then Left(PieceDefinitionError.EmptyMovements)
    else Right(movements)

  private def parseUniqueMovementNames(
      movements: MovementDefinitions
  ): Either[PieceDefinitionError, MovementDefinitions] =
    movements.foldLeft[Either[PieceDefinitionError, Set[MovementName]]](Right(Set.empty)) {
      (acc, movement) =>
        acc.flatMap: seen =>
          if seen.contains(movement.name) then Left(PieceDefinitionError.DuplicateMovementName(movement.name))
          else Right(seen + movement.name)
    }.map(_ => movements)

  private def resolveInitialMovement(
      movements: MovementDefinitions,
      initialMovement: Option[MovementName]
  ): Either[PieceDefinitionError, MovementName] =
    val movementNames = movements.names

    initialMovement match
      case None =>
        movements.headOption
          .map(_.name)
          .toRight(PieceDefinitionError.EmptyMovements)
      case Some(value) =>
        if movementNames.contains(value) then Right(value)
        else Left(PieceDefinitionError.InitialMovementNotFound(value))

  private def parseTransitionTargets(
      movements: MovementDefinitions
  ): Either[PieceDefinitionError, MovementDefinitions] =
    val movementNames = movements.names

    movements.foldLeft[Either[PieceDefinitionError, MovementDefinitions]](Right(MovementDefinitions.Empty)) {
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
    movement.rules.foldLeft[Either[PieceDefinitionError, MovementRules]](Right(MovementRules.Empty)) {
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
