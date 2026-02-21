package zain.core.takt.movement

import zain.core.takt.primitives.FacetName

final case class MovementFacets private (
    persona: Option[FacetName],
    policies: Vector[FacetName],
    knowledge: Vector[FacetName],
    instruction: Option[FacetName],
    outputContracts: Vector[FacetName]
)

object MovementFacets:
  val Empty: MovementFacets = MovementFacets(
    persona = None,
    policies = Vector.empty,
    knowledge = Vector.empty,
    instruction = None,
    outputContracts = Vector.empty
  )

  def create(
      persona: Option[FacetName],
      policies: Vector[FacetName],
      knowledge: Vector[FacetName],
      instruction: Option[FacetName],
      outputContracts: Vector[FacetName]
  ): MovementFacets =
    MovementFacets(
      persona = persona,
      policies = policies,
      knowledge = knowledge,
      instruction = instruction,
      outputContracts = outputContracts
    )
