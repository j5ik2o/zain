package zain.core.takt.inventory

final case class ReferenceModelInventory private (
    private val targets: Map[ReferenceModelTarget, ReferenceSourcePaths],
    private val exclusions: Map[String, ExclusionReason]
):
  def registerTarget(
      target: ReferenceModelTarget,
      sourcePaths: Vector[String]
  ): Either[ReferenceModelInventoryError, ReferenceModelInventory] =
    if sourcePaths.isEmpty then Left(ReferenceModelInventoryError.MissingReferenceSources(target))
    else
      parseSourcePaths(target, sourcePaths).map: parsedSourcePaths =>
        copy(targets = targets.updated(target, parsedSourcePaths))

  def registerExclusion(
      excludedName: String,
      reason: String
  ): Either[ReferenceModelInventoryError, ReferenceModelInventory] =
    ExclusionReason.create(excludedName, reason).map: exclusionReason =>
      copy(exclusions = exclusions.updated(excludedName, exclusionReason))

  def referencesOf(target: ReferenceModelTarget): Option[ReferenceSourcePaths] =
    targets.get(target)

  def exclusionOf(excludedName: String): Option[ExclusionReason] =
    exclusions.get(excludedName)

  def registeredTargets: Set[ReferenceModelTarget] =
    targets.keySet

  private def parseSourcePaths(
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
        acc.flatMap(_.registerTarget(target, sourcePaths))
    }
