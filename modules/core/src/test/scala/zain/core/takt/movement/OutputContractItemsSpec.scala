package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError

final class OutputContractItemsSpec extends AnyFunSuite:
  test("should create output contract item with default useJudge"):
    val actual = OutputContractItem.create(
      name = parseOutputContractName("00-plan.md"),
      format = parseOutputContractFormat("plan"),
      useJudge = None,
      order = None
    )

    assert(actual.exists(_.useJudge))

  test("should reject output contract item with empty name"):
    val actual = OutputContractName.parse("")

    assert(actual == Left(PieceDefinitionError.EmptyOutputContractName))

  test("should reject output contract item with empty format"):
    val actual = OutputContractFormat.parse("")

    assert(actual == Left(PieceDefinitionError.EmptyOutputContractFormat))

  test("should reject output contract item with empty order"):
    val actual = OutputContractOrder.parse("")

    assert(actual == Left(PieceDefinitionError.EmptyOutputContractOrder))

  test("should reject duplicate output contract item names"):
    val first = parseOutputContractItem("00-plan.md")
    val second = parseOutputContractItem("00-plan.md")

    val actual = OutputContractItems.create(Vector(first, second))

    assert(actual == Left(PieceDefinitionError.DuplicateOutputContractItemName("00-plan.md")))

  private def parseOutputContractItem(name: String): OutputContractItem =
    OutputContractItem.create(
      name = parseOutputContractName(name),
      format = parseOutputContractFormat("plan"),
      useJudge = Some(true),
      order = None
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"output contract item parsing should succeed: $error")

  private def parseOutputContractName(value: String): OutputContractName =
    OutputContractName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"output contract name parsing should succeed: $error")

  private def parseOutputContractFormat(value: String): OutputContractFormat =
    OutputContractFormat.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"output contract format parsing should succeed: $error")
