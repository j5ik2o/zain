package zain.core.takt.piece

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.facet.FacetCatalog
import zain.core.takt.movement.MovementDefinition
import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.movement.MovementFacets
import zain.core.takt.movement.MovementRule
import zain.core.takt.movement.MovementRules
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final class PieceDefinitionFactorySpec extends AnyFunSuite:
  private val pieceName = parsePieceName("references-takt-always-valid")
  private val planMovement = createMovement("plan")
  private val implementMovement = createMovement("implement")
  private val verifyMovement = parseMovementName("verify")

  test("should create piece when at least one movement exists"):
    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = Some(planMovement.name),
        maxMovements = Some(1)
      )
    )

    assert(actual.isRight)

  test("should reject piece creation when movements are empty"):
    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.Empty,
        initialMovement = None,
        maxMovements = None
      )
    )

    assert(actual == Left(PieceDefinitionError.EmptyMovements))

  test("should use first movement as initial movement when initial movement is omitted"):
    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement, implementMovement)),
        initialMovement = None,
        maxMovements = Some(5)
      )
    )

    assert(actual.exists(_.initialMovement == planMovement.name))

  test("should reject initial movement when it does not exist in movements"):
    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement, implementMovement)),
        initialMovement = Some(verifyMovement),
        maxMovements = Some(5)
      )
    )

    assert(actual == Left(PieceDefinitionError.InitialMovementNotFound(verifyMovement)))

  test("should use default max movements when max movements is omitted"):
    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = None,
        maxMovements = None
      )
    )

    assert(actual.exists(_.maxMovements.value == 10))

  test("should reject non positive max movements"):
    val zeroActual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = None,
        maxMovements = Some(0)
      )
    )
    val negativeActual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = None,
        maxMovements = Some(-1)
      )
    )

    assert(zeroActual == Left(PieceDefinitionError.NonPositiveMaxMovements))
    assert(negativeActual == Left(PieceDefinitionError.NonPositiveMaxMovements))

  test("should reject piece creation when movement names are duplicated"):
    val duplicatedMovementName = parseMovementName("plan")
    val firstMovement = createMovementWithTransition(name = "plan", next = "COMPLETE")
    val secondMovement = createMovementWithTransition(name = "plan", next = "COMPLETE")

    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(firstMovement, secondMovement)),
        initialMovement = Some(duplicatedMovementName),
        maxMovements = Some(5)
      )
    )

    assert(actual == Left(PieceDefinitionError.DuplicateMovementName(duplicatedMovementName)))

  test("should reject piece creation when transition target movement does not exist"):
    val unknownTarget = parseMovementName("unknown")
    val movement = createMovementWithTransition(name = "plan", next = "unknown")

    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(movement)),
        initialMovement = Some(movement.name),
        maxMovements = Some(5)
      )
    )

    assert(actual == Left(PieceDefinitionError.UndefinedTransitionTarget(target = unknownTarget, from = movement.name)))

  test("should apply default loop detection when loop detection is omitted"):
    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = Some(planMovement.name),
        maxMovements = Some(5)
      )
    )

    assert(actual.exists(_.loopDetection == LoopDetectionConfiguration.Default))

  test("should reject piece creation when loop monitor cycle references undefined movement"):
    val unknown = parseMovementName("unknown")
    val monitor = parseLoopMonitorConfiguration(
      cycle = Vector(planMovement.name, unknown),
      threshold = 2,
      rules = Vector(parseLoopMonitorRule("continue", "plan"))
    )

    val actual = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement, implementMovement)),
        initialMovement = Some(planMovement.name),
        maxMovements = Some(5),
        loopMonitors = LoopMonitorConfigurations.create(Vector(monitor))
      )
    )

    assert(actual == Left(PieceDefinitionError.UndefinedLoopMonitorCycleMovement(unknown)))

  test("should return same failure category for same undefined initial movement input"):
    val first = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = Some(verifyMovement),
        maxMovements = Some(5)
      )
    )
    val second = PieceDefinitionFactory.create(
      PieceDraft(
        name = pieceName,
        movements = MovementDefinitions.create(Vector(planMovement)),
        initialMovement = Some(verifyMovement),
        maxMovements = Some(5)
      )
    )

    assert(first == Left(PieceDefinitionError.InitialMovementNotFound(verifyMovement)))
    assert(second == first)

  private def parsePieceName(value: String): PieceName =
    PieceName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"piece name parsing should succeed: $error")

  private def parseMovementName(value: String): MovementName =
    MovementName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement name parsing should succeed: $error")

  private def parseRule(condition: String, next: Option[String]): MovementRule =
    MovementRule.create(condition, next) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"rule parsing should succeed: $error")

  private def createMovement(name: String): MovementDefinition =
    createMovementWithTransition(name = name, next = "COMPLETE")

  private def createMovementWithTransition(name: String, next: String): MovementDefinition =
    MovementDefinition.createTopLevel(
      name = parseMovementName(name),
      rules = MovementRules.create(Vector(parseRule(condition = "ok", next = Some(next)))),
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      hasParallel = false,
      hasArpeggio = false,
      teamLeader = None
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement creation should succeed: $error")

  private def parseLoopMonitorConfiguration(
      cycle: Vector[MovementName],
      threshold: Int,
      rules: Vector[LoopMonitorRule]
  ): LoopMonitorConfiguration =
    LoopMonitorConfiguration.create(
      cycle = cycle,
      threshold = threshold,
      judge = parseLoopMonitorJudge(rules)
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor configuration should succeed: $error")

  private def parseLoopMonitorJudge(rules: Vector[LoopMonitorRule]): LoopMonitorJudge =
    val parsedRules = LoopMonitorRules.create(rules) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor rules should succeed: $error")

    LoopMonitorJudge(
      persona = None,
      instructionTemplate = None,
      rules = parsedRules
    )

  private def parseLoopMonitorRule(condition: String, next: String): LoopMonitorRule =
    LoopMonitorRule.create(condition = condition, next = next) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor rule should succeed: $error")
