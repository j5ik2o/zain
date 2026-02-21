package zain.core.takt.movement

import zain.core.takt.facet.FacetCatalog
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.MovementName

final case class MovementDefinitionInput(
    name: MovementName,
    rules: MovementRules,
    facets: MovementFacets,
    facetCatalog: FacetCatalog,
    executionMode: MovementExecutionMode,
    parallel: Option[ParallelConfiguration] = None,
    arpeggio: Option[ArpeggioConfiguration] = None,
    outputContractItems: OutputContractItems = OutputContractItems.Empty
)

object MovementDefinitionInput:
  @deprecated(
    "Use MovementDefinitionInput(..., executionMode = ..., parallel = ..., arpeggio = ...) instead.",
    "0.1.0"
  )
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
        executionMode = executionMode,
        parallel = if hasParallel then Some(ParallelConfiguration.Empty) else None,
        arpeggio = if hasArpeggio then Some(ArpeggioConfiguration.Default) else None,
        outputContractItems = OutputContractItems.Empty
      )
