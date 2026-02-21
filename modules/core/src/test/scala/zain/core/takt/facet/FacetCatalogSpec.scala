package zain.core.takt.facet

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.FacetName

final class FacetCatalogSpec extends AnyFunSuite:
  test("should create catalog from definitions and parse existing references"):
    val persona = parseFacetName("persona")
    val policy = parseFacetName("policy")
    val knowledge = parseFacetName("knowledge")
    val instruction = parseFacetName("instruction")
    val outputContract = parseFacetName("output-contract")

    val catalog = FacetCatalog.fromDefinitions(
      personas = FacetNames.create(Vector(persona)),
      policies = FacetNames.create(Vector(policy)),
      knowledge = FacetNames.create(Vector(knowledge)),
      instructions = FacetNames.create(Vector(instruction)),
      outputContracts = FacetNames.create(Vector(outputContract))
    )
    val references = FacetReferences.create(
      persona = Some(persona),
      policies = FacetNames.create(Vector(policy)),
      knowledge = FacetNames.create(Vector(knowledge)),
      instruction = Some(instruction),
      outputContracts = FacetNames.create(Vector(outputContract))
    )

    val actual = catalog.parseReferences(references)

    assert(actual == Right(references))

  test("should reject undefined policy reference for catalog created from definitions"):
    val knownPolicy = parseFacetName("known-policy")
    val unknownPolicy = parseFacetName("unknown-policy")
    val catalog = FacetCatalog.fromDefinitions(
      personas = FacetNames.Empty,
      policies = FacetNames.create(Vector(knownPolicy)),
      knowledge = FacetNames.Empty,
      instructions = FacetNames.Empty,
      outputContracts = FacetNames.Empty
    )
    val references = FacetReferences.create(
      persona = None,
      policies = FacetNames.create(Vector(unknownPolicy)),
      knowledge = FacetNames.Empty,
      instruction = None,
      outputContracts = FacetNames.Empty
    )

    val actual = catalog.parseReferences(references)

    assert(actual == Left(PieceDefinitionError.UndefinedPolicyReference(unknownPolicy)))

  private def parseFacetName(value: String): FacetName =
    FacetName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"facet name parsing should succeed: $error")
