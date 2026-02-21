package zain.core.takt.piece

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.facet.FacetCatalog
import zain.core.takt.movement.MovementDefinition
import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.movement.MovementFacets
import zain.core.takt.movement.MovementRule
import zain.core.takt.movement.MovementRules
import zain.core.takt.piece.MovementOutput
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName
import zain.core.takt.primitives.TransitionTarget

final class PieceExecutionStateSpec extends AnyFunSuite:
  private val planMovementName = parseMovementName("plan")
  private val implementMovementName = parseMovementName("implement")
  private val pieceDefinition = createPieceDefinition()

  test("should start running state from piece definition"):
    val actual = PieceExecutionState.start(pieceDefinition)

    assert(actual.status == PieceExecutionStatus.Running)
    assert(actual.currentMovement == planMovementName)
    assert(actual.iteration.value == 0)

  test("should transition to completed when COMPLETE target is applied"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.transitionTo(target = TransitionTarget.Complete)

    assert(actual.exists(_.status == PieceExecutionStatus.Completed))

  test("should transition to aborted when ABORT target is applied"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.transitionTo(target = TransitionTarget.Abort)

    assert(actual.exists(_.status == PieceExecutionStatus.Aborted))

  test("should mark state completed with dedicated API"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.markCompleted

    assert(actual.exists(_.status == PieceExecutionStatus.Completed))

  test("should mark state aborted with dedicated API"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.markAborted

    assert(actual.exists(_.status == PieceExecutionStatus.Aborted))

  test("should return already finished when marking completed twice"):
    val started = PieceExecutionState.start(pieceDefinition)
    val completed = started.markCompleted match
      case Right(state) => state
      case Left(error)  => fail(s"mark completed should succeed: $error")

    val actual = completed.markCompleted

    assert(actual == Left(PieceExecutionError.AlreadyFinished(PieceExecutionStatus.Completed)))

  test("should return new instance when iteration is updated with dedicated API"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.withIteration(started.iteration.increment)

    assert(actual.iteration.value == 1)
    assert(started ne actual)

  test("should transition to defined movement and increment iteration"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.transitionTo(target = TransitionTarget.Movement(implementMovementName))

    assert(actual.exists(_.status == PieceExecutionStatus.Running))
    assert(actual.exists(_.currentMovement == implementMovementName))
    assert(actual.exists(_.iteration.value == 1))

  test("should return new instance when transition is applied"):
    val started = PieceExecutionState.start(pieceDefinition)

    val transitioned = started.transitionTo(target = TransitionTarget.Movement(implementMovementName)) match
      case Right(state) => state
      case Left(error)  => fail(s"transition should succeed: $error")

    assert(started ne transitioned)

  test("should reject transition to undefined movement target"):
    val started = PieceExecutionState.start(pieceDefinition)
    val undefinedMovementName = parseMovementName("unknown")

    val actual = started.transitionTo(target = TransitionTarget.Movement(undefinedMovementName))

    assert(actual == Left(PieceExecutionError.UndefinedTransitionMovement(undefinedMovementName)))

  test("should reject transition when state is already completed"):
    val started = PieceExecutionState.start(pieceDefinition)
    val completed = started.transitionTo(target = TransitionTarget.Complete) match
      case Right(state) => state
      case Left(error)  => fail(s"transition should succeed: $error")

    val actual = completed.transitionTo(target = TransitionTarget.Movement(implementMovementName))

    assert(actual == Left(PieceExecutionError.AlreadyFinished(PieceExecutionStatus.Completed)))

  test("should record movement output and keep last output immutably"):
    val started = PieceExecutionState.start(pieceDefinition)
    val output = parseMovementOutput(matchedRuleIndex = Some(0))

    val actual = started.recordMovementOutput(planMovementName, output)

    assert(actual.movementOutputs.get(planMovementName).contains(output))
    assert(actual.lastOutput.contains(output))
    assert(started.lastOutput.isEmpty)
    assert(started ne actual)

  test("should append user input immutably"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.appendUserInput("input")

    assert(actual.userInputs == Vector("input"))
    assert(started.userInputs.isEmpty)
    assert(started ne actual)

  test("should record persona session immutably"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.recordPersonaSession("reviewer", "session-1")

    assert(actual.personaSessions.get("reviewer").contains("session-1"))
    assert(started.personaSessions.isEmpty)
    assert(started ne actual)

  test("should increment movement iteration immutably"):
    val started = PieceExecutionState.start(pieceDefinition)

    val actual = started.incrementMovementIteration(planMovementName)

    assert(actual.movementIterations.get(planMovementName).exists(_.value == 1))
    assert(started.movementIterations.isEmpty)
    assert(started ne actual)

  test("should transition by matched rule index"):
    val started = PieceExecutionState.start(pieceDefinition)
    val currentMovement = movementByName(pieceDefinition, planMovementName)

    val actual = started.transitionByMatchedRuleIndex(currentMovement, matchedRuleIndex = 0)

    assert(actual.exists(_.currentMovement == implementMovementName))

  test("should reject transition by matched rule index when rule index is invalid"):
    val started = PieceExecutionState.start(pieceDefinition)
    val currentMovement = movementByName(pieceDefinition, planMovementName)

    val actual = started.transitionByMatchedRuleIndex(currentMovement, matchedRuleIndex = 999)

    assert(actual == Left(PieceExecutionError.InvalidRuleIndex(999)))

  private def createPieceDefinition(): PieceDefinition =
    val planMovement = createMovement("plan", next = "implement")
    val implementMovement = createMovement("implement", next = "verify")
    val verifyMovement = createMovement("verify", next = "COMPLETE")

    PieceDefinitionFactory.create(
      PieceDraft(
        name = parsePieceName("references-takt-always-valid"),
        movements = MovementDefinitions.create(Vector(planMovement, implementMovement, verifyMovement)),
        initialMovement = Some(planMovementName),
        maxMovements = Some(10)
      )
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"piece creation should succeed: $error")

  private def createMovement(name: String, next: String): MovementDefinition =
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

  private def parseMovementOutput(matchedRuleIndex: Option[Int]): MovementOutput =
    MovementOutput.create(
      content = "output",
      matchedRuleIndex = matchedRuleIndex
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement output parsing should succeed: $error")

  private def movementByName(pieceDefinition: PieceDefinition, movementName: MovementName): MovementDefinition =
    pieceDefinition.movements.breachEncapsulationOfValues.find(_.name == movementName) match
      case Some(movement) => movement
      case None           => fail(s"movement should exist: ${movementName.value}")
