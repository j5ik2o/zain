package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError

final class ArpeggioConfigurationSpec extends AnyFunSuite:
  test("should create arpeggio configuration when values are positive"):
    val actual = ArpeggioConfiguration.create(
      batchSize = 1,
      concurrency = 2
    )

    assert(actual.exists(_.batchSize == 1))
    assert(actual.exists(_.concurrency == 2))

  test("should reject non positive batch size"):
    val actual = ArpeggioConfiguration.create(
      batchSize = 0,
      concurrency = 1
    )

    assert(actual == Left(PieceDefinitionError.NonPositiveArpeggioBatchSize))

  test("should reject non positive concurrency"):
    val actual = ArpeggioConfiguration.create(
      batchSize = 1,
      concurrency = 0
    )

    assert(actual == Left(PieceDefinitionError.NonPositiveArpeggioConcurrency))
