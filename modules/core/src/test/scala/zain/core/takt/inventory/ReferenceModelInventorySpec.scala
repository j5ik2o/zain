package zain.core.takt.inventory

import org.scalatest.funsuite.AnyFunSuite

final class ReferenceModelInventorySpec extends AnyFunSuite:
  test("should register all migration targets with at least one source reference"):
    val actual = ReferenceModelInventory.fromReferencesTakt

    assert(actual.isRight)
    val inventory = actual match
      case Right(value) => value
      case Left(_)      => fail("inventory creation should succeed")
    val expectedTargets = Set(
      ReferenceModelTarget.PieceDefinition,
      ReferenceModelTarget.MovementDefinition,
      ReferenceModelTarget.TransitionRule,
      ReferenceModelTarget.OutputContract,
      ReferenceModelTarget.PartDefinition,
      ReferenceModelTarget.ExecutionState
    )
    assert(inventory.registeredTargets == expectedTargets)
    assert(expectedTargets.forall(target => inventory.referencesOf(target).exists(_.nonEmpty)))

  test("should reject target registration when source references are empty"):
    val actual = ReferenceModelInventory.Empty.registerTarget(
      target = ReferenceModelTarget.PieceDefinition,
      sourcePaths = ReferenceSourcePaths.Empty
    )

    assert(actual == Left(ReferenceModelInventoryError.MissingReferenceSources(ReferenceModelTarget.PieceDefinition)))

  test("should reject target registration when source reference path is empty"):
    val actual = ReferenceModelInventory.parseSourcePaths(
      target = ReferenceModelTarget.OutputContract,
      sourcePaths = Vector("")
    )

    assert(actual == Left(ReferenceModelInventoryError.EmptyReferenceSourcePath(ReferenceModelTarget.OutputContract)))

  test("should require exclusion reason for non migration target"):
    val excludedName = parseExcludedName("legacy-type")
    val actual = ExclusionReason.create(
      excludedName = excludedName,
      value = ""
    )

    assert(actual == Left(ReferenceModelInventoryError.MissingExclusionReason(excludedName)))

  test("should register exclusion when exclusion reason is provided"):
    val excludedName = parseExcludedName("legacy-type")
    val exclusionReason = parseExclusionReason(
      excludedName = excludedName,
      value = "external provider type"
    )
    val actual = ReferenceModelInventory.Empty.registerExclusion(
      excludedName = excludedName,
      reason = exclusionReason
    )

    assert(actual.isRight)
    val inventory = actual match
      case Right(value) => value
      case Left(_)      => fail("exclusion registration should succeed")
    val reason = inventory.exclusionOf(excludedName)
    assert(reason.exists(_.value == "external provider type"))

  private def parseExcludedName(value: String): ExcludedName =
    ExcludedName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"excluded name parsing should succeed: $error")

  private def parseExclusionReason(
      excludedName: ExcludedName,
      value: String
  ): ExclusionReason =
    ExclusionReason.create(
      excludedName = excludedName,
      value = value
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"exclusion reason parsing should succeed: $error")
