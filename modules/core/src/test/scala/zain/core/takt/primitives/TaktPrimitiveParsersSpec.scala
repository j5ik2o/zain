package zain.core.takt.primitives

import org.scalatest.funsuite.AnyFunSuite

final class TaktPrimitiveParsersSpec extends AnyFunSuite:
  test("should reject empty facet name"):
    val actual = TaktPrimitiveParsers.parseFacetName("")

    assert(actual == Left(TaktPrimitiveError.EmptyFacetName))

  test("should allow one character facet name"):
    val actual = TaktPrimitiveParsers.parseFacetName("f")

    assert(actual.exists(_.value == "f"))

  test("should reject empty movement identifier"):
    val actual = TaktPrimitiveParsers.parseMovementIdentifier("")

    assert(actual == Left(TaktPrimitiveError.EmptyMovementIdentifier))

  test("should allow one character movement identifier"):
    val actual = TaktPrimitiveParsers.parseMovementIdentifier("a")

    assert(actual.exists(_.value == "a"))

  test("should reject empty piece name"):
    val actual = TaktPrimitiveParsers.parsePieceName("")

    assert(actual == Left(TaktPrimitiveError.EmptyPieceName))

  test("should allow one character piece name"):
    val actual = TaktPrimitiveParsers.parsePieceName("p")

    assert(actual.exists(_.value == "p"))

  test("should reject empty part id"):
    val actual = TaktPrimitiveParsers.parsePartId("")

    assert(actual == Left(TaktPrimitiveError.EmptyPartId))

  test("should allow one character part id"):
    val actual = TaktPrimitiveParsers.parsePartId("1")

    assert(actual.exists(_.value == "1"))

  test("should reject empty part title"):
    val actual = TaktPrimitiveParsers.parsePartTitle("")

    assert(actual == Left(TaktPrimitiveError.EmptyPartTitle))

  test("should allow one character part title"):
    val actual = TaktPrimitiveParsers.parsePartTitle("t")

    assert(actual.exists(_.value == "t"))

  test("should reject empty part instruction"):
    val actual = TaktPrimitiveParsers.parsePartInstruction("")

    assert(actual == Left(TaktPrimitiveError.EmptyPartInstruction))

  test("should allow one character part instruction"):
    val actual = TaktPrimitiveParsers.parsePartInstruction("i")

    assert(actual.exists(_.value == "i"))

  test("should reject non positive part timeout millis"):
    val zeroActual = TaktPrimitiveParsers.parsePartTimeoutMillis(0)
    val negativeActual = TaktPrimitiveParsers.parsePartTimeoutMillis(-1)

    assert(zeroActual == Left(TaktPrimitiveError.NonPositivePartTimeoutMillis))
    assert(negativeActual == Left(TaktPrimitiveError.NonPositivePartTimeoutMillis))

  test("should allow positive part timeout millis"):
    val actual = TaktPrimitiveParsers.parsePartTimeoutMillis(1)

    assert(actual.exists(_.value == 1))

  test("should reject non positive team leader max parts"):
    val actual = TaktPrimitiveParsers.parseTeamLeaderMaxParts(0)

    assert(actual == Left(TaktPrimitiveError.NonPositiveTeamLeaderMaxParts))

  test("should reject team leader max parts exceeding upper limit"):
    val actual = TaktPrimitiveParsers.parseTeamLeaderMaxParts(TeamLeaderMaxParts.UpperLimit + 1)

    assert(actual == Left(TaktPrimitiveError.TeamLeaderMaxPartsExceedsLimit))

  test("should allow team leader max parts within upper limit"):
    val actual = TaktPrimitiveParsers.parseTeamLeaderMaxParts(TeamLeaderMaxParts.UpperLimit)

    assert(actual.exists(_.value == TeamLeaderMaxParts.UpperLimit))

  test("should reject non positive team leader timeout millis"):
    val actual = TaktPrimitiveParsers.parseTeamLeaderTimeoutMillis(0)

    assert(actual == Left(TaktPrimitiveError.NonPositiveTeamLeaderTimeoutMillis))

  test("should allow positive team leader timeout millis"):
    val actual = TaktPrimitiveParsers.parseTeamLeaderTimeoutMillis(1)

    assert(actual.exists(_.value == 1))

  test("should reject empty rule condition"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("")

    assert(actual == Left(TaktPrimitiveError.EmptyRuleCondition))

  test("should allow one character rule condition"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("x")

    assert(actual.exists(_.breachEncapsulationOfRawValue == "x"))

  test("should parse ai condition expression"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("""ai("approved")""")

    assert(actual.exists(_.aiConditionText.contains("approved")))

  test("should parse all condition expression"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("""all("approved")""")

    assert(actual.exists {
      _.aggregateCondition.contains(
        zain.core.takt.primitives.RuleCondition.AggregateType.All -> Vector("approved")
      )
    })

  test("should parse any condition expression with multiple arguments"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("""any("approved", "needs_fix")""")

    assert(actual.exists {
      _.aggregateCondition.contains(
        zain.core.takt.primitives.RuleCondition.AggregateType.Any -> Vector("approved", "needs_fix")
      )
    })

  test("should reject malformed ai condition expression"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("ai(approved)")

    assert(actual == Left(TaktPrimitiveError.InvalidRuleConditionSyntax))

  test("should reject malformed aggregate condition expression"):
    val actual = TaktPrimitiveParsers.parseRuleCondition("all(approved)")

    assert(actual == Left(TaktPrimitiveError.InvalidRuleConditionSyntax))

  test("should reject empty transition target"):
    val actual = TaktPrimitiveParsers.parseTransitionTarget("")

    assert(actual == Left(TaktPrimitiveError.EmptyTransitionTarget))

  test("should allow one character transition target"):
    val actual = TaktPrimitiveParsers.parseTransitionTarget("q")

    assert(actual.exists {
      case TransitionTarget.Movement(name) => name.value == "q"
      case _                               => false
    })

  test("should parse COMPLETE transition target as terminal transition"):
    val actual = TaktPrimitiveParsers.parseTransitionTarget("COMPLETE")

    assert(actual.contains(TransitionTarget.Complete))

  test("should parse ABORT transition target as terminal transition"):
    val actual = TaktPrimitiveParsers.parseTransitionTarget("ABORT")

    assert(actual.contains(TransitionTarget.Abort))

  test("should reject non positive max movements"):
    val zeroActual = TaktPrimitiveParsers.parseMaxMovements(0)
    val negativeActual = TaktPrimitiveParsers.parseMaxMovements(-1)

    assert(zeroActual == Left(TaktPrimitiveError.NonPositiveMaxMovements))
    assert(negativeActual == Left(TaktPrimitiveError.NonPositiveMaxMovements))

  test("should allow positive max movements"):
    val actual = TaktPrimitiveParsers.parseMaxMovements(1)

    assert(actual.exists(_.value == 1))

  test("should reject negative iteration count"):
    val actual = TaktPrimitiveParsers.parseIterationCount(-1)

    assert(actual == Left(TaktPrimitiveError.NegativeIterationCount))

  test("should allow zero iteration count"):
    val actual = TaktPrimitiveParsers.parseIterationCount(0)

    assert(actual.exists(_.value == 0))

  test("should return same failure category for same invalid max movements input"):
    val first = TaktPrimitiveParsers.parseMaxMovements(0)
    val second = TaktPrimitiveParsers.parseMaxMovements(0)

    assert(first == Left(TaktPrimitiveError.NonPositiveMaxMovements))
    assert(second == first)

  test("should return same failure category for same invalid iteration input"):
    val first = TaktPrimitiveParsers.parseIterationCount(-1)
    val second = TaktPrimitiveParsers.parseIterationCount(-1)

    assert(first == Left(TaktPrimitiveError.NegativeIterationCount))
    assert(second == first)
