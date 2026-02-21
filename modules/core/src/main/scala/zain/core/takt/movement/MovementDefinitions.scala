package zain.core.takt.movement

import zain.core.takt.primitives.MovementName

final case class MovementDefinitions private (
    private val values: Vector[MovementDefinition]
):
  def isEmpty: Boolean =
    values.isEmpty

  def headOption: Option[MovementDefinition] =
    values.headOption

  def names: Set[MovementName] =
    values.map(_.name).toSet

  def foldLeft[B](initial: B)(operation: (B, MovementDefinition) => B): B =
    values.foldLeft(initial)(operation)

  def :+(movement: MovementDefinition): MovementDefinitions =
    MovementDefinitions(values :+ movement)

  def breachEncapsulationOfValues: Vector[MovementDefinition] =
    values

object MovementDefinitions:
  val Empty: MovementDefinitions = MovementDefinitions(Vector.empty)

  def create(values: Vector[MovementDefinition]): MovementDefinitions =
    MovementDefinitions(values)
