package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError

final class ArpeggioConfigurationSpec extends AnyFunSuite:
  test("should create arpeggio configuration when values are positive"):
    val actual = ArpeggioConfiguration.create(
      batchSize = parseArpeggioBatchSize(1),
      concurrency = parseArpeggioConcurrency(2)
    )

    assert(actual.exists(_.batchSize.value == 1))
    assert(actual.exists(_.concurrency.value == 2))

  test("should reject non positive batch size"):
    val actual = ArpeggioBatchSize.parse(0)

    assert(actual == Left(PieceDefinitionError.NonPositiveArpeggioBatchSize))

  test("should reject non positive concurrency"):
    val actual = ArpeggioConcurrency.parse(0)

    assert(actual == Left(PieceDefinitionError.NonPositiveArpeggioConcurrency))

  private def parseArpeggioBatchSize(value: Int): ArpeggioBatchSize =
    ArpeggioBatchSize.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"arpeggio batch size parsing should succeed: $error")

  private def parseArpeggioConcurrency(value: Int): ArpeggioConcurrency =
    ArpeggioConcurrency.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"arpeggio concurrency parsing should succeed: $error")
