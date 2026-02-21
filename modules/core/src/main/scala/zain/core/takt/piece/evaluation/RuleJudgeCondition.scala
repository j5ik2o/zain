package zain.core.takt.piece.evaluation

final case class RuleJudgeCondition(
    index: RuleJudgeConditionIndex,
    text: RuleJudgeConditionText
)

object RuleJudgeCondition:
  def parse(
      index: Int,
      text: String
  ): Either[zain.core.takt.piece.PieceExecutionError, RuleJudgeCondition] =
    for
      parsedIndex <- RuleJudgeConditionIndex.parse(index)
      parsedText <- RuleJudgeConditionText.parse(text)
    yield RuleJudgeCondition(
      index = parsedIndex,
      text = parsedText
    )

  def create(
      index: Int,
      text: String
  ): Either[zain.core.takt.piece.PieceExecutionError, RuleJudgeCondition] =
    parse(
      index = index,
      text = text
    )
