package zain.core.takt.facet

final case class FacetReferenceEntries private (
    private val values: Vector[FacetReferenceEntry]
):
  def ++(other: FacetReferenceEntries): FacetReferenceEntries =
    FacetReferenceEntries(values ++ other.values)

  def foldLeft[B](initial: B)(operation: (B, FacetReferenceEntry) => B): B =
    values.foldLeft(initial)(operation)

  def breachEncapsulationOfValues: Vector[FacetReferenceEntry] =
    values

object FacetReferenceEntries:
  val Empty: FacetReferenceEntries = FacetReferenceEntries(Vector.empty)

  def create(values: Vector[FacetReferenceEntry]): FacetReferenceEntries =
    FacetReferenceEntries(values)

  def one(value: FacetReferenceEntry): FacetReferenceEntries =
    FacetReferenceEntries(Vector(value))
