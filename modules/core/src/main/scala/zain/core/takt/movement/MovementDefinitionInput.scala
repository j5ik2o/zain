package zain.core.takt.movement

import zain.core.takt.facet.FacetCatalog
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.MovementName

final case class MovementDefinitionInput(
    name: MovementName,
    rules: MovementRules,
    facets: MovementFacets,
    facetCatalog: FacetCatalog,
    executionMode: MovementExecutionMode
)

object MovementDefinitionInput:
  def fromLegacy(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      hasParallel: Boolean,
      hasArpeggio: Boolean,
      teamLeader: Option[TeamLeaderConfiguration]
  ): Either[PieceDefinitionError, MovementDefinitionInput] =
    MovementExecutionMode.resolve(
      hasParallel = hasParallel,
      hasArpeggio = hasArpeggio,
      teamLeader = teamLeader
    ).map: executionMode =>
      MovementDefinitionInput(
        name = name,
        rules = rules,
        facets = facets,
        facetCatalog = facetCatalog,
        executionMode = executionMode
      )
