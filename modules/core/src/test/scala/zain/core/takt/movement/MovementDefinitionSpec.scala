package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.facet.FacetCatalog
import zain.core.takt.facet.FacetNames
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
      rules = movementRulesOf(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )
    val parallelOnly = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Parallel
    )
    val teamLeaderOnly = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(rule),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.TeamLeader(teamLeader)
    )

    assert(noMode.isRight)
    assert(parallelOnly.isRight)
    assert(teamLeaderOnly.isRight)

  test("should create top level movement from scoped input API"):
    val input = MovementDefinitionInput(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
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
      rules = movementRulesOf(parseRule(condition = "ok", next = None)),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    val actual = MovementDefinition.create(
      input = input,
      scope = MovementScope.Nested
    )

    assert(actual.isRight)

  test("should resolve execution mode from legacy style flags"):
    val actual = MovementExecutionMode.resolve(
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    )

    assert(actual == Right(MovementExecutionMode.Sequential))

  test("should reject execution mode resolve when multiple execution modes are selected"):
    val teamLeader = createTeamLeaderConfiguration(maxParts = 2, timeoutMillis = 90000)

    val parallelAndArpeggio = MovementExecutionMode.resolve(
      hasParallel = true,
      hasArpeggio = true,
      teamLeader = None
    )
    val parallelAndTeamLeader = MovementExecutionMode.resolve(
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
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )
    val parallel = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Parallel
    )
    val arpeggio = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Arpeggio
    )
    val withTeamLeader = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.TeamLeader(teamLeader)
    )

    assert(sequential.isRight)
    assert(parallel.isRight)
    assert(arpeggio.isRight)
    assert(withTeamLeader.isRight)

  test("should keep parallel configuration when execution mode is parallel"):
    val reviewer = MovementDefinition.createNested(
      name = parseMovementName("reviewer"),
      rules = movementRulesOf(parseRule(condition = "ok", next = None)),
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      executionMode = MovementExecutionMode.Sequential
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"nested movement creation should succeed: $error")

    val parallelConfiguration = ParallelConfiguration.create(
      MovementDefinitions.create(Vector(reviewer))
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      executionMode = MovementExecutionMode.Parallel,
      parallel = Some(parallelConfiguration)
    )

    assert(actual.exists(_.parallel.contains(parallelConfiguration)))

  test("should reject sequential mode when parallel configuration is supplied"):
    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      executionMode = MovementExecutionMode.Sequential,
      parallel = Some(ParallelConfiguration.Empty)
    )

    assert(actual == Left(PieceDefinitionError.InvalidExecutionModeConfiguration))

  test("should keep arpeggio configuration when execution mode is arpeggio"):
    val arpeggioConfiguration = ArpeggioConfiguration.create(
      batchSize = 2,
      concurrency = 3
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"arpeggio configuration should succeed: $error")

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      executionMode = MovementExecutionMode.Arpeggio,
      arpeggio = Some(arpeggioConfiguration)
    )

    assert(actual.exists(_.arpeggio.contains(arpeggioConfiguration)))

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
      rules = movementRulesOf(ruleWithoutNext),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual == Left(PieceDefinitionError.MissingTopLevelRuleTransitionTarget))

  test("should allow nested movement to omit rule transition target"):
    val ruleWithoutNext = parseRule(condition = "ok", next = None)

    val actual = MovementDefinition.createNested(
      name = movementName,
      rules = movementRulesOf(ruleWithoutNext),
      facets = emptyFacets,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual.isRight)

  test("should return same failure category for same conflicting execution mode resolve input"):
    val first = MovementExecutionMode.resolve(
      hasParallel = true,
      hasArpeggio = true,
      teamLeader = None
    )
    val second = MovementExecutionMode.resolve(
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
      personas = FacetNames.Empty,
      policies = facetNamesOf(knownPolicy),
      knowledge = FacetNames.Empty,
      instructions = FacetNames.Empty,
      outputContracts = FacetNames.Empty
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = facetNamesOf(unknownPolicy),
      knowledge = FacetNames.Empty,
      instruction = None,
      outputContracts = FacetNames.Empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual == Left(PieceDefinitionError.UndefinedPolicyReference(unknownPolicy)))

  test("should reject movement creation when persona reference is undefined"):
    val unknownPersona = parseFacetName("unknown-persona")
    val facetCatalog = FacetCatalog.create(
      personas = facetNamesOf(parseFacetName("known-persona")),
      policies = FacetNames.Empty,
      knowledge = FacetNames.Empty,
      instructions = FacetNames.Empty,
      outputContracts = FacetNames.Empty
    )
    val facets = MovementFacets.create(
      persona = Some(unknownPersona),
      policies = FacetNames.Empty,
      knowledge = FacetNames.Empty,
      instruction = None,
      outputContracts = FacetNames.Empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual == Left(PieceDefinitionError.UndefinedPersonaReference(unknownPersona)))

  test("should reject movement creation when knowledge reference is undefined"):
    val unknownKnowledge = parseFacetName("unknown-knowledge")
    val facetCatalog = FacetCatalog.create(
      personas = FacetNames.Empty,
      policies = FacetNames.Empty,
      knowledge = facetNamesOf(parseFacetName("known-knowledge")),
      instructions = FacetNames.Empty,
      outputContracts = FacetNames.Empty
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = FacetNames.Empty,
      knowledge = facetNamesOf(unknownKnowledge),
      instruction = None,
      outputContracts = FacetNames.Empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual == Left(PieceDefinitionError.UndefinedKnowledgeReference(unknownKnowledge)))

  test("should reject movement creation when instruction reference is undefined"):
    val unknownInstruction = parseFacetName("unknown-instruction")
    val facetCatalog = FacetCatalog.create(
      personas = FacetNames.Empty,
      policies = FacetNames.Empty,
      knowledge = FacetNames.Empty,
      instructions = facetNamesOf(parseFacetName("known-instruction")),
      outputContracts = FacetNames.Empty
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = FacetNames.Empty,
      knowledge = FacetNames.Empty,
      instruction = Some(unknownInstruction),
      outputContracts = FacetNames.Empty
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual == Left(PieceDefinitionError.UndefinedInstructionReference(unknownInstruction)))

  test("should reject movement creation when output contract reference is undefined"):
    val unknownOutputContract = parseFacetName("unknown-output-contract")
    val facetCatalog = FacetCatalog.create(
      personas = FacetNames.Empty,
      policies = FacetNames.Empty,
      knowledge = FacetNames.Empty,
      instructions = FacetNames.Empty,
      outputContracts = facetNamesOf(parseFacetName("known-output-contract"))
    )
    val facets = MovementFacets.create(
      persona = None,
      policies = FacetNames.Empty,
      knowledge = FacetNames.Empty,
      instruction = None,
      outputContracts = facetNamesOf(unknownOutputContract)
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual == Left(PieceDefinitionError.UndefinedOutputContractReference(unknownOutputContract)))

  test("should keep output contract items on movement definition"):
    val outputContract = parseOutputContractItem(name = "00-plan.md", format = "plan")

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = MovementFacets.Empty,
      facetCatalog = emptyFacetCatalog,
      executionMode = MovementExecutionMode.Sequential,
      outputContractItems = outputContractItemsOf(outputContract)
    )

    assert(actual.exists(_.outputContractItems.breachEncapsulationOfValues.map(_.name) == Vector("00-plan.md")))

  test("should create movement when all facet references are defined"):
    val persona = parseFacetName("persona")
    val policy = parseFacetName("policy")
    val knowledge = parseFacetName("knowledge")
    val instruction = parseFacetName("instruction")
    val outputContract = parseFacetName("output-contract")
    val facetCatalog = FacetCatalog.create(
      personas = facetNamesOf(persona),
      policies = facetNamesOf(policy),
      knowledge = facetNamesOf(knowledge),
      instructions = facetNamesOf(instruction),
      outputContracts = facetNamesOf(outputContract)
    )
    val facets = MovementFacets.create(
      persona = Some(persona),
      policies = facetNamesOf(policy),
      knowledge = facetNamesOf(knowledge),
      instruction = Some(instruction),
      outputContracts = facetNamesOf(outputContract)
    )

    val actual = MovementDefinition.createTopLevel(
      name = movementName,
      rules = movementRulesOf(parseRule(condition = "ok", next = Some("verify"))),
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = MovementExecutionMode.Sequential
    )

    assert(actual.isRight)

  private def movementRulesOf(values: MovementRule*): MovementRules =
    MovementRules.create(values.toVector)

  private def outputContractItemsOf(values: OutputContractItem*): OutputContractItems =
    OutputContractItems.create(values.toVector) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"output contract items parsing should succeed: $error")

  private def parseOutputContractItem(name: String, format: String): OutputContractItem =
    OutputContractItem.create(
      name = name,
      format = format,
      useJudge = None,
      order = None
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"output contract item parsing should succeed: $error")

  private def facetNamesOf(values: FacetName*): FacetNames =
    FacetNames.create(values.toVector)

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
