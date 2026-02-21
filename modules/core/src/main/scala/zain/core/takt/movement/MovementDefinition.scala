package zain.core.takt.movement

import zain.core.takt.facet.FacetCatalog
import zain.core.takt.facet.FacetReferences
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.MovementName

final case class MovementDefinition private (
    name: MovementName,
    rules: MovementRules,
    facets: MovementFacets,
    executionMode: MovementExecutionMode
)

object MovementDefinition:
  def create(
      input: MovementDefinitionInput,
      scope: MovementScope
  ): Either[PieceDefinitionError, MovementDefinition] =
    scope match
      case MovementScope.TopLevel =>
        createTopLevel(
          name = input.name,
          rules = input.rules,
          facets = input.facets,
          facetCatalog = input.facetCatalog,
          executionMode = input.executionMode
        )
      case MovementScope.Nested =>
        createNested(
          name = input.name,
          rules = input.rules,
          facets = input.facets,
          facetCatalog = input.facetCatalog,
          executionMode = input.executionMode
        )

  def createTopLevel(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode
  ): Either[PieceDefinitionError, MovementDefinition] =
    create(
      name = name,
      rules = rules,
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = executionMode,
      requireTransitionTarget = true
    )

  def createTopLevel(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      hasParallel: Boolean,
      hasArpeggio: Boolean,
      teamLeader: Option[TeamLeaderConfiguration]
  ): Either[PieceDefinitionError, MovementDefinition] =
    MovementExecutionMode.resolve(
      hasParallel = hasParallel,
      hasArpeggio = hasArpeggio,
      teamLeader = teamLeader
    ).flatMap: executionMode =>
      createTopLevel(
        name = name,
        rules = rules,
        facets = facets,
        facetCatalog = facetCatalog,
        executionMode = executionMode
      )

  def createNested(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode
  ): Either[PieceDefinitionError, MovementDefinition] =
    create(
      name = name,
      rules = rules,
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = executionMode,
      requireTransitionTarget = false
    )

  def createNested(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      hasParallel: Boolean,
      hasArpeggio: Boolean,
      teamLeader: Option[TeamLeaderConfiguration]
  ): Either[PieceDefinitionError, MovementDefinition] =
    MovementExecutionMode.resolve(
      hasParallel = hasParallel,
      hasArpeggio = hasArpeggio,
      teamLeader = teamLeader
    ).flatMap: executionMode =>
      createNested(
        name = name,
        rules = rules,
        facets = facets,
        facetCatalog = facetCatalog,
        executionMode = executionMode
      )

  private def create(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode,
      requireTransitionTarget: Boolean
  ): Either[PieceDefinitionError, MovementDefinition] =
    for
      parsedRules <- parseRuleTransitionTargets(
        rules = rules,
        requireTransitionTarget = requireTransitionTarget
      )
      parsedFacetReferences <- facetCatalog.parseReferences(toFacetReferences(facets))
    yield MovementDefinition(
      name = name,
      rules = parsedRules,
      facets = toMovementFacets(parsedFacetReferences),
      executionMode = executionMode
    )

  private def parseRuleTransitionTargets(
      rules: MovementRules,
      requireTransitionTarget: Boolean
  ): Either[PieceDefinitionError, MovementRules] =
    if !requireTransitionTarget || rules.forall(_.next.nonEmpty) then Right(rules)
    else Left(PieceDefinitionError.MissingTopLevelRuleTransitionTarget)

  private def toFacetReferences(facets: MovementFacets): FacetReferences =
    FacetReferences.create(
      persona = facets.persona,
      policies = facets.policies,
      knowledge = facets.knowledge,
      instruction = facets.instruction,
      outputContracts = facets.outputContracts
    )

  private def toMovementFacets(references: FacetReferences): MovementFacets =
    MovementFacets.create(
      persona = references.persona,
      policies = references.policies,
      knowledge = references.knowledge,
      instruction = references.instruction,
      outputContracts = references.outputContracts
    )
