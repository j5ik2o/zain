package zain.core.takt.movement

final case class MovementRules private (
    private val values: Vector[MovementRule]
):
  def isEmpty: Boolean =
    values.isEmpty

  def nonEmpty: Boolean =
    values.nonEmpty

  def size: Int =
    values.size

  def indices: Range =
    values.indices

  def ruleAt(index: Int): Option[MovementRule] =
    values.lift(index)

  def forall(predicate: MovementRule => Boolean): Boolean =
    values.forall(predicate)

  def foldLeft[B](initial: B)(operation: (B, MovementRule) => B): B =
    values.foldLeft(initial)(operation)

  def :+(rule: MovementRule): MovementRules =
    MovementRules(values :+ rule)

  def breachEncapsulationOfValues: Vector[MovementRule] =
    values

object MovementRules:
  val Empty: MovementRules = MovementRules(Vector.empty)

  def create(values: Vector[MovementRule]): MovementRules =
    MovementRules(values)
