package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError

final class TeamLeaderConfigurationSpec extends AnyFunSuite:
  test("should create team leader configuration when max parts and timeout are valid"):
    val actual = TeamLeaderConfiguration.create(
      maxParts = 3,
      timeoutMillis = 600000
    )

    assert(actual.exists(_.maxParts.value == 3))
    assert(actual.exists(_.timeoutMillis.value == 600000))

  test("should reject team leader configuration when max parts is out of range"):
    val zeroActual = TeamLeaderConfiguration.create(
      maxParts = 0,
      timeoutMillis = 600000
    )
    val overLimitActual = TeamLeaderConfiguration.create(
      maxParts = 4,
      timeoutMillis = 600000
    )

    assert(zeroActual == Left(PieceDefinitionError.TeamLeaderMaxPartsOutOfRange))
    assert(overLimitActual == Left(PieceDefinitionError.TeamLeaderMaxPartsOutOfRange))

  test("should reject team leader configuration when timeout is non positive"):
    val actual = TeamLeaderConfiguration.create(
      maxParts = 2,
      timeoutMillis = 0
    )

    assert(actual == Left(PieceDefinitionError.NonPositiveTeamLeaderTimeoutMillis))

  test("should return same failure category for same invalid max parts input"):
    val first = TeamLeaderConfiguration.create(
      maxParts = 4,
      timeoutMillis = 600000
    )
    val second = TeamLeaderConfiguration.create(
      maxParts = 4,
      timeoutMillis = 600000
    )

    assert(first == Left(PieceDefinitionError.TeamLeaderMaxPartsOutOfRange))
    assert(second == first)
