package zain.core.takt.facet

import zain.core.takt.primitives.FacetName

final case class FacetReferenceEntry private (
    category: FacetCategory,
    reference: FacetName
)

object FacetReferenceEntry:
  def of(category: FacetCategory, reference: FacetName): FacetReferenceEntry =
    FacetReferenceEntry(
      category = category,
      reference = reference
    )
