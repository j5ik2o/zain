package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.facet.FacetCatalog
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.FacetName
import zain.core.takt.primitives.MovementName

final class MovementDefinitionSpec extends AnyFunSuite:
  private val movementName = parseMovementName("implement")
  private val emptyFacetCatalog = FacetCatalog.Empty
  private val emptyFacets = MovementFacets.Empty

  test("should create top level movement when at most one execution mode is selected"):
    val rule = parseRule(condition = "ok", next = Some("verify"))
    val teamLeader = createTeamLeaderConfiguration(maxParts = 2, timeoutMillis = 90000)
    val noMode = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )
    val parallelOnly = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = true,
      hasArpeggio = false,
      teamLeader = None
    )
    val teamLeaderOnly = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = Some(teamLeader)
    )

    assert(noMode.isRight)
    assert(parallelOnly.isRight)
    assert(teamLeaderOnly.isRight)

  test("should create top level movement from scoped input API"):
    val input = MovementDefinitionInput(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    val actual = MovementDefinition.create(
      input = input,
      scope = MovementScope.TopLevel
    )

    assert(actual.isRight)

  test("should allow nested movement from scoped input API to omit transition target"):
    val input = MovementDefinitionInput(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = None)),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    val actual = MovementDefinition.create(
      input = input,
      scope = MovementScope.Nested
    )

    assert(actual.isRight)

  test("should map legacy execution mode fields to explicit execution mode"):
    val actual = MovementDefinitionInput.fromLegacy(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual.exists(_.executionMode == MovementExecutionMode.Sequential))

  test("should reject top level movement when multiple execution modes are selected"):
    val rule = parseRule(condition = "ok", next = Some("verify"))
    val teamLeader = createTeamLeaderConfiguration(maxParts = 2, timeoutMillis = 90000)

    val parallelAndArpeggio = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = true,
      hasArpeggio = true,
      teamLeader = None
    )
    val parallelAndTeamLeader = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = true,
      hasArpeggio = false,
      teamLeader = Some(teamLeader)
    )

    assert(parallelAndArpeggio == Left(PieceDefinitionError.ConflictingExecutionModes))
    assert(parallelAndTeamLeader == Left(PieceDefinitionError.ConflictingExecutionModes))

  test("should create top level movement with explicit execution mode API"):
    val teamLeader = createTeamLeaderConfiguration(maxParts = 2, timeoutMillis = 90000)

    val sequential = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )
    val parallel = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Parallel
    )
    val arpeggio = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Arpeggio
    )
    val withTeamLeader = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.TeamLeader(teamLeader)
    )

    assert(sequential.isRight)
    assert(parallel.isRight)
    assert(arpeggio.isRight)
    assert(withTeamLeader.isRight)

  test("should reject rule creation when condition is empty"):
    val actual = MovementRule.create(
      condition = "",
      next = Some("verify")
    )

    assert(actual == Left(PieceDefinitionError.EmptyRuleCondition))

  test("should reject rule creation when transition target is empty"):
    val actual = MovementRule.create(
      condition = "ok",
      next = Some("")
    )

    assert(actual == Left(PieceDefinitionError.EmptyRuleTransitionTarget))

  test("should reject top level movement when rule transition target is omitted"):
    val ruleWithoutNext = parseRule(condition = "ok", next = None)

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(ruleWithoutNext),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Left(PieceDefinitionError.MissingTopLevelRuleTransitionTarget))

  test("should allow nested movement to omit rule transition target"):
    val ruleWithoutNext = parseRule(condition = "ok", next = None)

    val actual = MovementDefinition.createNested(
      name = movementName,
      rules = Vector(ruleWithoutNext),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual.isRight)

  test("should return same failure category for same conflicting execution mode input"):
    val rule = parseRule(condition = "ok", next = Some("verify"))

    val first = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = true,
      hasArpeggio = true,
      teamLeader = None
    )
    val second = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      hasParallel = true,
      hasArpeggio = true,
      teamLeader = None
    )

    assert(first == Left(PieceDefinitionError.ConflictingExecutionModes))
    assert(second == first)

  test("should reject movement creation when policy reference is undefined"):
    val knownPolicy = parseFacetName("coding")
    val unknownPolicy = parseFacetName("missing")
    val facetCatalog = FacetCatalog.create(
      personas = Vector.empty,
      policies = Vector(knownPolicy),
      knowledge = Vector.empty,
      instructions = Vector.empty,
      outputContracts = Vector.empty
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = Vector(unknownPolicy),
      knowledge = Vector.empty,
      instruction = None,
      outputContracts = Vector.empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Left(PieceDefinitionError.UndefinedPolicyReference(unknownPolicy)))

  test("should reject movement creation when persona reference is undefined"):
    val unknownPersona = parseFacetName("unknown-persona")
    val facetCatalog = FacetCatalog.create(
      personas = Vector(parseFacetName("known-persona")),
      policies = Vector.empty,
      knowledge = Vector.empty,
      instructions = Vector.empty,
      outputContracts = Vector.empty
    )
    val facets = MovementFacets.create(
      persona = Some(unknownPersona),
      policies = Vector.empty,
      knowledge = Vector.empty,
      instruction = None,
      outputContracts = Vector.empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Left(PieceDefinitionError.UndefinedPersonaReference(unknownPersona)))

  test("should reject movement creation when knowledge reference is undefined"):
    val unknownKnowledge = parseFacetName("unknown-knowledge")
    val facetCatalog = FacetCatalog.create(
      personas = Vector.empty,
      policies = Vector.empty,
      knowledge = Vector(parseFacetName("known-knowledge")),
      instructions = Vector.empty,
      outputContracts = Vector.empty
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = Vector.empty,
      knowledge = Vector(unknownKnowledge),
      instruction = None,
      outputContracts = Vector.empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Left(PieceDefinitionError.UndefinedKnowledgeReference(unknownKnowledge)))

  test("should reject movement creation when instruction reference is undefined"):
    val unknownInstruction = parseFacetName("unknown-instruction")
    val facetCatalog = FacetCatalog.create(
      personas = Vector.empty,
      policies = Vector.empty,
      knowledge = Vector.empty,
      instructions = Vector(parseFacetName("known-instruction")),
      outputContracts = Vector.empty
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = Vector.empty,
      knowledge = Vector.empty,
      instruction = Some(unknownInstruction),
      outputContracts = Vector.empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Left(PieceDefinitionError.UndefinedInstructionReference(unknownInstruction)))

  test("should reject movement creation when output contract reference is undefined"):
    val unknownOutputContract = parseFacetName("unknown-output-contract")
    val facetCatalog = FacetCatalog.create(
      personas = Vector.empty,
      policies = Vector.empty,
      knowledge = Vector.empty,
      instructions = Vector.empty,
      outputContracts = Vector(parseFacetName("known-output-contract"))
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = Vector.empty,
      knowledge = Vector.empty,
      instruction = None,
      outputContracts = Vector(unknownOutputContract)
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Left(PieceDefinitionError.UndefinedOutputContractReference(unknownOutputContract)))

  test("should create movement when all facet references are defined"):
    val persona = parseFacetName("persona")
    val policy = parseFacetName("policy")
    val knowledge = parseFacetName("knowledge")
    val instruction = parseFacetName("instruction")
    val outputContract = parseFacetName("output-contract")
    val facetCatalog = FacetCatalog.create(
      personas = Vector(persona),
      policies = Vector(policy),
      knowledge = Vector(knowledge),
      instructions = Vector(instruction),
      outputContracts = Vector(outputContract)
    )
    val facets = MovementFacets.create(
      persona = Some(persona),
      policies = Vector(policy),
      knowledge = Vector(knowledge),
      instruction = Some(instruction),
      outputContracts = Vector(outputContract)
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = Vector(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual.isRight)

  private def createTeamLeaderConfiguration(maxParts: Int, timeoutMillis: Int): TeamLeaderConfiguration =
    TeamLeaderConfiguration.create(maxParts, timeoutMillis) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"team leader configuration should succeed: $error")

  private def parseMovementName(value: String): MovementName =
    MovementName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement name parsing should succeed: $error")

  private def parseFacetName(value: String): FacetName =
    FacetName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"facet name parsing should succeed: $error")

  private def parseRule(condition: String, next: Option[String]): MovementRule =
    MovementRule.create(condition, next) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"rule parsing should succeed: $error")
