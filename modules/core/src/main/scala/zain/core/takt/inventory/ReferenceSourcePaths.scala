package zain.core.takt.inventory

final case class ReferenceSourcePaths private (
    private val values: Vector[ReferenceSourcePath]
):
  def isEmpty: Boolean =
    values.isEmpty

  def nonEmpty: Boolean =
    values.nonEmpty

  def :+(value: ReferenceSourcePath): ReferenceSourcePaths =
    ReferenceSourcePaths(values :+ value)

  def breachEncapsulationOfValues: Vector[ReferenceSourcePath] =
    values

object ReferenceSourcePaths:
  val Empty: ReferenceSourcePaths = ReferenceSourcePaths(Vector.empty)

  def create(values: Vector[ReferenceSourcePath]): ReferenceSourcePaths =
    ReferenceSourcePaths(values)
