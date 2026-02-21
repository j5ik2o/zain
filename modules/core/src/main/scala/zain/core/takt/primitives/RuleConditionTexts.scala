package zain.core.takt.primitives

final case class RuleConditionTexts private (
    private val values: Vector[RuleConditionText]
):
  def map[B](transform: RuleConditionText => B): Vector[B] =
    values.map(transform)

  def isEmpty: Boolean =
    values.isEmpty

  def breachEncapsulationOfValues: Vector[RuleConditionText] =
    values

object RuleConditionTexts:
  val Empty: RuleConditionTexts = RuleConditionTexts(Vector.empty)

  def create(values: Vector[RuleConditionText]): RuleConditionTexts =
    RuleConditionTexts(values)
