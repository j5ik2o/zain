package zain.core.takt.piece

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.MovementNames

final class LoopMonitorConfigurationsSpec extends AnyFunSuite:
  private val plan = parseMovementName("plan")
  private val implement = parseMovementName("implement")

  test("should allow loop monitor when cycle and judge targets are defined"):
    val monitor = parseLoopMonitorConfiguration(
      cycle = Vector(plan, implement),
      threshold = 2,
      rules = Vector(
        parseLoopMonitorRule("continue", "plan"),
        parseLoopMonitorRule("stop", "COMPLETE")
      )
    )
    val monitors = LoopMonitorConfigurations.create(Vector(monitor))

    val actual = monitors.parseDefinedMovements(Set(plan, implement))

    assert(actual.isRight)

  test("should reject loop monitor when cycle includes undefined movement"):
    val unknown = parseMovementName("unknown")
    val monitor = parseLoopMonitorConfiguration(
      cycle = Vector(plan, unknown),
      threshold = 2,
      rules = Vector(parseLoopMonitorRule("continue", "plan"))
    )
    val monitors = LoopMonitorConfigurations.create(Vector(monitor))

    val actual = monitors.parseDefinedMovements(Set(plan, implement))

    assert(actual == Left(PieceDefinitionError.UndefinedLoopMonitorCycleMovement(unknown)))

  test("should reject loop monitor when judge target includes undefined movement"):
    val unknown = parseMovementName("unknown")
    val monitor = parseLoopMonitorConfiguration(
      cycle = Vector(plan, implement),
      threshold = 2,
      rules = Vector(parseLoopMonitorRule("continue", "unknown"))
    )
    val monitors = LoopMonitorConfigurations.create(Vector(monitor))

    val actual = monitors.parseDefinedMovements(Set(plan, implement))

    assert(actual == Left(PieceDefinitionError.UndefinedLoopMonitorJudgeTarget(unknown)))

  test("should reject loop monitor configuration when cycle is empty"):
    val judge = parseLoopMonitorJudge(Vector(parseLoopMonitorRule("continue", "plan")))

    val actual = LoopMonitorConfiguration.create(
      cycle = MovementNames.Empty,
      threshold = parseLoopMonitorThreshold(2),
      judge = judge
    )

    assert(actual == Left(PieceDefinitionError.EmptyLoopMonitorCycle))

  test("should reject loop monitor configuration when threshold is non positive"):
    val actual = LoopMonitorThreshold.parse(0)

    assert(actual == Left(PieceDefinitionError.NonPositiveLoopMonitorThreshold))

  test("should reject loop monitor configuration when cycle has only one movement"):
    val judge = parseLoopMonitorJudge(Vector(parseLoopMonitorRule("continue", "plan")))

    val actual = LoopMonitorConfiguration.create(
      cycle = MovementNames.create(Vector(plan)),
      threshold = parseLoopMonitorThreshold(2),
      judge = judge
    )

    assert(actual == Left(PieceDefinitionError.LoopMonitorCycleRequiresAtLeastTwoMovements))

  test("should reject loop monitor judge when rules are empty"):
    val actual = LoopMonitorRules.create(Vector.empty)

    assert(actual == Left(PieceDefinitionError.EmptyLoopMonitorJudgeRules))

  private def parseLoopMonitorConfiguration(
      cycle: Vector[MovementName],
      threshold: Int,
      rules: Vector[LoopMonitorRule]
  ): LoopMonitorConfiguration =
    LoopMonitorConfiguration.create(
      cycle = MovementNames.create(cycle),
      threshold = parseLoopMonitorThreshold(threshold),
      judge = parseLoopMonitorJudge(rules)
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor configuration parsing should succeed: $error")

  private def parseLoopMonitorThreshold(value: Int): LoopMonitorThreshold =
    LoopMonitorThreshold.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor threshold parsing should succeed: $error")

  private def parseLoopMonitorJudge(rules: Vector[LoopMonitorRule]): LoopMonitorJudge =
    val parsedRules = LoopMonitorRules.create(rules) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor judge rule parsing should succeed: $error")

    LoopMonitorJudge(
      persona = None,
      instructionTemplate = None,
      rules = parsedRules
    )

  private def parseLoopMonitorRule(condition: String, next: String): LoopMonitorRule =
    LoopMonitorRule.create(
      condition = condition,
      next = next
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"loop monitor rule parsing should succeed: $error")

  private def parseMovementName(value: String): MovementName =
    MovementName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement name parsing should succeed: $error")
