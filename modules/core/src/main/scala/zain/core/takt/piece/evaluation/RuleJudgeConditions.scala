package zain.core.takt.piece.evaluation

final case class RuleJudgeConditions private (
    private val values: Vector[RuleJudgeCondition]
):
  def size: Int =
    values.size

  def isEmpty: Boolean =
    values.isEmpty

  def breachEncapsulationOfValues: Vector[RuleJudgeCondition] =
    values

object RuleJudgeConditions:
  val Empty: RuleJudgeConditions = RuleJudgeConditions(Vector.empty)

  def create(values: Vector[RuleJudgeCondition]): RuleJudgeConditions =
    RuleJudgeConditions(values)
