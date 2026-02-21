package zain.core.takt.movement

import zain.core.takt.facet.FacetNames
import zain.core.takt.primitives.FacetName

final case class MovementFacets private (
    persona: Option[FacetName],
    policies: FacetNames,
    knowledge: FacetNames,
    instruction: Option[FacetName],
    outputContracts: FacetNames
)

object MovementFacets:
  val Empty: MovementFacets = MovementFacets(
    persona = None,
    policies = FacetNames.Empty,
    knowledge = FacetNames.Empty,
    instruction = None,
    outputContracts = FacetNames.Empty
  )

  def create(
      persona: Option[FacetName],
      policies: FacetNames,
      knowledge: FacetNames,
      instruction: Option[FacetName],
      outputContracts: FacetNames
  ): MovementFacets =
    MovementFacets(
      persona = persona,
      policies = policies,
      knowledge = knowledge,
      instruction = instruction,
      outputContracts = outputContracts
    )
