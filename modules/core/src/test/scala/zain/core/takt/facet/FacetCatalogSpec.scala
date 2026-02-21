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
      personas = Vector(persona),
      policies = Vector(policy),
      knowledge = Vector(knowledge),
      instructions = Vector(instruction),
      outputContracts = Vector(outputContract)
    )
    val references = FacetReferences.create(
      persona = Some(persona),
      policies = Vector(policy),
      knowledge = Vector(knowledge),
      instruction = Some(instruction),
      outputContracts = Vector(outputContract)
    )

    val actual = catalog.parseReferences(references)

    assert(actual == Right(references))

  test("should reject undefined policy reference for catalog created from definitions"):
    val knownPolicy = parseFacetName("known-policy")
    val unknownPolicy = parseFacetName("unknown-policy")
    val catalog = FacetCatalog.fromDefinitions(
      personas = Vector.empty,
      policies = Vector(knownPolicy),
      knowledge = Vector.empty,
      instructions = Vector.empty,
      outputContracts = Vector.empty
    )
    val references = FacetReferences.create(
      persona = None,
      policies = Vector(unknownPolicy),
      knowledge = Vector.empty,
      instruction = None,
      outputContracts = Vector.empty
    )

    val actual = catalog.parseReferences(references)

    assert(actual == Left(PieceDefinitionError.UndefinedPolicyReference(unknownPolicy)))

  private def parseFacetName(value: String): FacetName =
    FacetName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"facet name parsing should succeed: $error")
