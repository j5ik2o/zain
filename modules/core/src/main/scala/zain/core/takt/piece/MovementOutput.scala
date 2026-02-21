package zain.core.takt.piece

final case class MovementOutput private (
    content: String,
    matchedRuleIndex: Option[Int]
)

object MovementOutput:
  def parse(
      content: String,
      matchedRuleIndex: Option[Int]
  ): Either[PieceExecutionError, MovementOutput] =
    parseMatchedRuleIndex(matchedRuleIndex).map: parsedMatchedRuleIndex =>
      MovementOutput(
        content = content,
        matchedRuleIndex = parsedMatchedRuleIndex
      )

  def create(
      content: String,
      matchedRuleIndex: Option[Int]
  ): Either[PieceExecutionError, MovementOutput] =
    parse(
      content = content,
      matchedRuleIndex = matchedRuleIndex
    )

  private def parseMatchedRuleIndex(
      matchedRuleIndex: Option[Int]
  ): Either[PieceExecutionError, Option[Int]] =
    matchedRuleIndex match
      case None => Right(None)
      case Some(value) =>
        if value < 0 then Left(PieceExecutionError.NegativeMatchedRuleIndex(value))
        else Right(Some(value))
