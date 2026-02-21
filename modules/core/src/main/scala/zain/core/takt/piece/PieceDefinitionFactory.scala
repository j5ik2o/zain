package zain.core.takt.piece

import zain.core.takt.primitives.MaxMovements

object PieceDefinitionFactory:
  private val DefaultMaxMovements = 10

  def create(draft: PieceDraft): Either[PieceDefinitionError, PieceDefinition] =
    for
      nonEmptyMovements <- draft.movements.parseNonEmpty
      uniqueMovements <- nonEmptyMovements.parseUniqueNames
      parsedMovements <- uniqueMovements.parseTransitionTargets
      resolvedInitial <- parsedMovements.parseInitialMovement(draft.initialMovement)
      resolvedMaxMovements <- resolveMaxMovements(draft.maxMovements)
      resolvedLoopDetection <- resolveLoopDetection(draft.loopDetection)
      resolvedLoopMonitors <- resolveLoopMonitors(
        loopMonitors = draft.loopMonitors,
        movementNames = parsedMovements.names
      )
    yield PieceDefinition.create(
      name = draft.name,
      movements = parsedMovements,
      initialMovement = resolvedInitial,
      maxMovements = resolvedMaxMovements,
      loopDetection = resolvedLoopDetection,
      loopMonitors = resolvedLoopMonitors
    )

  private def resolveMaxMovements(maxMovements: Option[Int]): Either[PieceDefinitionError, MaxMovements] =
    MaxMovements
      .parse(maxMovements.getOrElse(DefaultMaxMovements))
      .left
      .map(_ => PieceDefinitionError.NonPositiveMaxMovements)

  private def resolveLoopDetection(
      loopDetection: Option[LoopDetectionConfiguration]
  ): Either[PieceDefinitionError, LoopDetectionConfiguration] =
    loopDetection match
      case Some(value) => Right(value)
      case None        => Right(LoopDetectionConfiguration.Default)

  private def resolveLoopMonitors(
      loopMonitors: LoopMonitorConfigurations,
      movementNames: Set[zain.core.takt.primitives.MovementName]
  ): Either[PieceDefinitionError, LoopMonitorConfigurations] =
    loopMonitors.parseDefinedMovements(movementNames)
