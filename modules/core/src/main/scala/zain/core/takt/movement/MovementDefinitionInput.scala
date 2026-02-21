package zain.core.takt.movement

import zain.core.takt.facet.FacetCatalog
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
