package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError

final class OutputContractItemsSpec extends AnyFunSuite:
  test("should create output contract item with default useJudge"):
    val actual = OutputContractItem.create(
      name = "00-plan.md",
      format = "plan",
      useJudge = None,
      order = None
    )

    assert(actual.exists(_.useJudge))

  test("should reject output contract item with empty name"):
    val actual = OutputContractItem.create(
      name = "",
      format = "plan",
      useJudge = None,
      order = None
    )

    assert(actual == Left(PieceDefinitionError.EmptyOutputContractName))

  test("should reject output contract item with empty format"):
    val actual = OutputContractItem.create(
      name = "00-plan.md",
      format = "",
      useJudge = None,
      order = None
    )

    assert(actual == Left(PieceDefinitionError.EmptyOutputContractFormat))

  test("should reject output contract item with empty order"):
    val actual = OutputContractItem.create(
      name = "00-plan.md",
      format = "plan",
      useJudge = None,
      order = Some("")
    )

    assert(actual == Left(PieceDefinitionError.EmptyOutputContractOrder))

  test("should reject duplicate output contract item names"):
    val first = parseOutputContractItem("00-plan.md")
    val second = parseOutputContractItem("00-plan.md")

    val actual = OutputContractItems.create(Vector(first, second))

    assert(actual == Left(PieceDefinitionError.DuplicateOutputContractItemName("00-plan.md")))

  private def parseOutputContractItem(name: String): OutputContractItem =
    OutputContractItem.create(
      name = name,
      format = "plan",
      useJudge = Some(true),
      order = None
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"output contract item parsing should succeed: $error")
