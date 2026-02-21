package zain.core.takt.movement

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.piece.PieceDefinitionError

final class PartDefinitionSpec extends AnyFunSuite:
  test("should create part definition when all required fields are valid"):
    val actual = PartDefinition.create(
      id = "part-1",
      title = "Implementation",
      instruction = "Implement all tasks",
      timeoutMillis = Some(120000)
    )

    assert(actual.exists(_.id.value == "part-1"))
    assert(actual.exists(_.title.value == "Implementation"))
    assert(actual.exists(_.instruction.value == "Implement all tasks"))
    assert(actual.exists(_.timeoutMillis.exists(_.value == 120000)))

  test("should create part definition when timeout is omitted"):
    val actual = PartDefinition.create(
      id = "part-1",
      title = "Implementation",
      instruction = "Implement all tasks",
      timeoutMillis = None
    )

    assert(actual.exists(_.timeoutMillis.isEmpty))

  test("should reject part definition when id is empty"):
    val actual = PartDefinition.create(
      id = "",
      title = "Implementation",
      instruction = "Implement all tasks",
      timeoutMillis = None
    )

    assert(actual == Left(PieceDefinitionError.EmptyPartId))

  test("should reject part definition when title is empty"):
    val actual = PartDefinition.create(
      id = "part-1",
      title = "",
      instruction = "Implement all tasks",
      timeoutMillis = None
    )

    assert(actual == Left(PieceDefinitionError.EmptyPartTitle))

  test("should reject part definition when instruction is empty"):
    val actual = PartDefinition.create(
      id = "part-1",
      title = "Implementation",
      instruction = "",
      timeoutMillis = None
    )

    assert(actual == Left(PieceDefinitionError.EmptyPartInstruction))

  test("should reject part definition when timeout is non positive"):
    val actual = PartDefinition.create(
      id = "part-1",
      title = "Implementation",
      instruction = "Implement all tasks",
      timeoutMillis = Some(0)
    )

    assert(actual == Left(PieceDefinitionError.NonPositivePartTimeoutMillis))

  test("should return same failure category for same invalid part id input"):
    val first = PartDefinition.create(
      id = "",
      title = "Implementation",
      instruction = "Implement all tasks",
      timeoutMillis = None
    )
    val second = PartDefinition.create(
      id = "",
      title = "Implementation",
      instruction = "Implement all tasks",
      timeoutMillis = None
    )

    assert(first == Left(PieceDefinitionError.EmptyPartId))
    assert(second == first)
