package zain.core.takt.facet

import zain.core.takt.primitives.FacetName

final case class FacetNames private (
    private val values: Vector[FacetName]
):
  def map[B](transform: FacetName => B): Vector[B] =
    values.map(transform)

  def toSet: Set[FacetName] =
    values.toSet

  def toVector: Vector[FacetName] =
    values

object FacetNames:
  val Empty: FacetNames = FacetNames(Vector.empty)

  def create(values: Vector[FacetName]): FacetNames =
    FacetNames(values)
