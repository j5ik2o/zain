package zain.core.takt.movement

final case class MovementRules private (
    private val values: Vector[MovementRule]
):
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
