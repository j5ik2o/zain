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
    yield PieceDefinition.create(
      name = draft.name,
      movements = parsedMovements,
      initialMovement = resolvedInitial,
      maxMovements = resolvedMaxMovements
    )

  private def resolveMaxMovements(maxMovements: Option[Int]): Either[PieceDefinitionError, MaxMovements] =
    MaxMovements
      .parse(maxMovements.getOrElse(DefaultMaxMovements))
      .left
      .map(_ => PieceDefinitionError.NonPositiveMaxMovements)
