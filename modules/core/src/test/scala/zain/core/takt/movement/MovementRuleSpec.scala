package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.RuleCondition

final class MovementRuleSpec extends AnyFunSuite:
  test("should parse ai condition and keep interactive flags"):
    val actual = MovementRule.create(
      condition = """ai("approved")""",
      next = Some("COMPLETE"),
      appendix = Some("appendix"),
      requiresUserInput = true,
      interactiveOnly = true
    )

    assert(actual.exists(_.condition.aiConditionText.contains("approved")))
    assert(actual.exists(_.requiresUserInput))
    assert(actual.exists(_.interactiveOnly))

  test("should parse all aggregate condition"):
    val actual = MovementRule.create(
      condition = """all("approved")""",
      next = Some("COMPLETE"),
      appendix = None,
      requiresUserInput = false,
      interactiveOnly = false
    )

    assert(actual.exists {
      _.condition.aggregateCondition.contains(
        RuleCondition.AggregateType.All -> Vector("approved")
      )
    })

  test("should parse any aggregate condition with multiple arguments"):
    val actual = MovementRule.create(
      condition = """any("approved", "needs_fix")""",
      next = Some("COMPLETE"),
      appendix = None,
      requiresUserInput = false,
      interactiveOnly = false
    )

    assert(actual.exists {
      _.condition.aggregateCondition.contains(
        RuleCondition.AggregateType.Any -> Vector("approved", "needs_fix")
      )
    })

  test("should reject malformed aggregate condition syntax"):
    val actual = MovementRule.create(
      condition = "all(approved)",
      next = Some("COMPLETE"),
      appendix = None,
      requiresUserInput = false,
      interactiveOnly = false
    )

    assert(actual == Left(PieceDefinitionError.InvalidRuleCondition))

  test("should reject empty appendix when appendix is specified"):
    val actual = MovementRule.create(
      condition = "ok",
      next = Some("COMPLETE"),
      appendix = Some(""),
      requiresUserInput = false,
      interactiveOnly = false
    )

    assert(actual == Left(PieceDefinitionError.EmptyRuleAppendix))
