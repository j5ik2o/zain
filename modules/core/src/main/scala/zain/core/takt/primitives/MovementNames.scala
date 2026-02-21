package zain.core.takt.primitives

final case class MovementNames private (
    private val values: Vector[MovementName]
):
  def isEmpty: Boolean =
    values.isEmpty

  def size: Int =
    values.size

  def find(predicate: MovementName => Boolean): Option[MovementName] =
    values.find(predicate)

  def breachEncapsulationOfValues: Vector[MovementName] =
    values

object MovementNames:
  val Empty: MovementNames = MovementNames(Vector.empty)

  def create(values: Vector[MovementName]): MovementNames =
    MovementNames(values)
