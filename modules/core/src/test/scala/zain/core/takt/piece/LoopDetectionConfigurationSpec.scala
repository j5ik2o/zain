package zain.core.takt.piece

import org.scalatest.funsuite.AnyFunSuite

final class LoopDetectionConfigurationSpec extends AnyFunSuite:
  test("should create loop detection configuration with defaults"):
    val actual = LoopDetectionConfiguration.create(
      maxConsecutiveSameStep = None,
      action = None
    )

    assert(actual == Right(LoopDetectionConfiguration.Default))

  test("should reject non positive max consecutive same step"):
    val actual = LoopDetectionMaxConsecutiveSameStep.parse(0)

    assert(actual == Left(PieceDefinitionError.NonPositiveLoopDetectionMaxConsecutiveSameStep))
