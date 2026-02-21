package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.PartId
import zain.core.takt.primitives.PartInstruction
import zain.core.takt.primitives.PartTimeoutMillis
import zain.core.takt.primitives.PartTitle

final case class PartDefinition private (
    id: PartId,
    title: PartTitle,
    instruction: PartInstruction,
    timeoutMillis: Option[PartTimeoutMillis]
)

object PartDefinition:
  def create(
      id: String,
      title: String,
      instruction: String,
      timeoutMillis: Option[Int]
  ): Either[PieceDefinitionError, PartDefinition] =
    for
      parsedId <- PartId
        .parse(id)
        .left
        .map(_ => PieceDefinitionError.EmptyPartId)
      parsedTitle <- PartTitle
        .parse(title)
        .left
        .map(_ => PieceDefinitionError.EmptyPartTitle)
      parsedInstruction <- PartInstruction
        .parse(instruction)
        .left
        .map(_ => PieceDefinitionError.EmptyPartInstruction)
      parsedTimeoutMillis <- parseTimeoutMillis(timeoutMillis)
    yield PartDefinition(
      id = parsedId,
      title = parsedTitle,
      instruction = parsedInstruction,
      timeoutMillis = parsedTimeoutMillis
    )

  private def parseTimeoutMillis(
      timeoutMillis: Option[Int]
  ): Either[PieceDefinitionError, Option[PartTimeoutMillis]] =
    timeoutMillis match
      case None => Right(None)
      case Some(value) =>
        PartTimeoutMillis
          .parse(value)
          .left
          .map(_ => PieceDefinitionError.NonPositivePartTimeoutMillis)
          .map(Some(_))
