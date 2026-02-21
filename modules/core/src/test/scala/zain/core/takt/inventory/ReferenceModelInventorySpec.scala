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
      sourcePaths = Vector.empty
    )

    assert(actual == Left(ReferenceModelInventoryError.MissingReferenceSources(ReferenceModelTarget.PieceDefinition)))

  test("should reject target registration when source reference path is empty"):
    val actual = ReferenceModelInventory.Empty.registerTarget(
      target = ReferenceModelTarget.OutputContract,
      sourcePaths = Vector("")
    )

    assert(actual == Left(ReferenceModelInventoryError.EmptyReferenceSourcePath(ReferenceModelTarget.OutputContract)))

  test("should require exclusion reason for non migration target"):
    val actual = ReferenceModelInventory.Empty.registerExclusion(
      excludedName = "legacy-type",
      reason = ""
    )

    assert(actual == Left(ReferenceModelInventoryError.MissingExclusionReason("legacy-type")))

  test("should register exclusion when exclusion reason is provided"):
    val actual = ReferenceModelInventory.Empty.registerExclusion(
      excludedName = "legacy-type",
      reason = "external provider type"
    )

    assert(actual.isRight)
    val inventory = actual match
      case Right(value) => value
      case Left(_)      => fail("exclusion registration should succeed")
    val exclusionReason = inventory.exclusionOf("legacy-type")
    assert(exclusionReason.exists(_.value == "external provider type"))
