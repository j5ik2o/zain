package zain.core.takt.inventory

final case class ReferenceModelInventory private (
    private val targets: Map[ReferenceModelTarget, ReferenceSourcePaths],
    private val exclusions: Map[ExcludedName, ExclusionReason]
):
  def registerTarget(
      target: ReferenceModelTarget,
      sourcePaths: ReferenceSourcePaths
  ): Either[ReferenceModelInventoryError, ReferenceModelInventory] =
    if sourcePaths.isEmpty then Left(ReferenceModelInventoryError.MissingReferenceSources(target))
    else Right(copy(targets = targets.updated(target, sourcePaths)))

  def registerExclusion(
      excludedName: ExcludedName,
      reason: ExclusionReason
  ): Either[ReferenceModelInventoryError, ReferenceModelInventory] =
    Right(copy(exclusions = exclusions.updated(excludedName, reason)))

  def referencesOf(target: ReferenceModelTarget): Option[ReferenceSourcePaths] =
    targets.get(target)

  def exclusionOf(excludedName: ExcludedName): Option[ExclusionReason] =
    exclusions.get(excludedName)

  def registeredTargets: Set[ReferenceModelTarget] =
    targets.keySet

object ReferenceModelInventory:
  val Empty: ReferenceModelInventory = ReferenceModelInventory(Map.empty, Map.empty)

  def fromReferencesTakt: Either[ReferenceModelInventoryError, ReferenceModelInventory] =
    val defaultMappings = Vector(
      ReferenceModelTarget.PieceDefinition -> Vector("references/takt/src/core/models/piece-types.ts"),
      ReferenceModelTarget.MovementDefinition -> Vector("references/takt/src/core/models/piece-types.ts"),
      ReferenceModelTarget.TransitionRule -> Vector("references/takt/src/core/models/piece-types.ts"),
      ReferenceModelTarget.OutputContract -> Vector("references/takt/src/core/models/piece-types.ts"),
      ReferenceModelTarget.PartDefinition -> Vector("references/takt/src/core/models/part.ts"),
      ReferenceModelTarget.ExecutionState -> Vector("references/takt/src/core/models/piece-types.ts")
    )

    defaultMappings.foldLeft[Either[ReferenceModelInventoryError, ReferenceModelInventory]](Right(Empty)) {
      case (acc, (target, sourcePaths)) =>
        for
          inventory <- acc
          parsedSourcePaths <- parseSourcePaths(target, sourcePaths)
          updated <- inventory.registerTarget(target, parsedSourcePaths)
        yield updated
    }

  def parseSourcePaths(
      target: ReferenceModelTarget,
      sourcePaths: Vector[String]
  ): Either[ReferenceModelInventoryError, ReferenceSourcePaths] =
    sourcePaths.foldLeft[Either[ReferenceModelInventoryError, ReferenceSourcePaths]](Right(ReferenceSourcePaths.Empty)) {
      case (acc, currentPath) =>
        for
          parsedPaths <- acc
          sourcePath <- ReferenceSourcePath.create(target, currentPath)
        yield parsedPaths :+ sourcePath
    }
