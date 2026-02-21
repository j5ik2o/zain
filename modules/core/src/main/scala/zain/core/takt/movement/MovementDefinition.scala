package zain.core.takt.movement

import zain.core.takt.facet.FacetCatalog
import zain.core.takt.facet.FacetReferences
import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.piece.PieceExecutionError
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.TransitionTarget

final case class MovementDefinition private (
    name: MovementName,
    rules: MovementRules,
    facets: MovementFacets,
    executionMode: MovementExecutionMode,
    parallel: Option[ParallelConfiguration],
    arpeggio: Option[ArpeggioConfiguration],
    outputContractItems: OutputContractItems
):
  def parseTransitionTargets(
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, MovementDefinition] =
    rules.foldLeft[Either[PieceDefinitionError, MovementRules]](Right(MovementRules.Empty)) {
      (acc, rule) =>
        for
          parsedRules <- acc
          parsedRule <- parseRuleTransitionTarget(rule, movementNames)
        yield parsedRules :+ parsedRule
    }.map(_ => this)

  private def parseRuleTransitionTarget(
      rule: MovementRule,
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, MovementRule] =
    rule.next match
      case Some(TransitionTarget.Movement(target)) =>
        if movementNames.contains(target) then Right(rule)
        else Left(PieceDefinitionError.UndefinedTransitionTarget(target = target, from = name))
      case _ =>
        Right(rule)

  def transitionTargetByMatchedRuleIndex(
      matchedRuleIndex: Int
  ): Either[PieceExecutionError, TransitionTarget] =
    rules.ruleAt(matchedRuleIndex) match
      case None =>
        Left(PieceExecutionError.InvalidRuleIndex(matchedRuleIndex))
      case Some(rule) =>
        rule.next match
          case Some(target) => Right(target)
          case None         => Left(PieceExecutionError.RuleWithoutTransitionTarget(matchedRuleIndex))

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
          executionMode = input.executionMode,
          parallel = input.parallel,
          arpeggio = input.arpeggio,
          outputContractItems = input.outputContractItems
        )
      case MovementScope.Nested =>
        createNested(
          name = input.name,
          rules = input.rules,
          facets = input.facets,
          facetCatalog = input.facetCatalog,
          executionMode = input.executionMode,
          parallel = input.parallel,
          arpeggio = input.arpeggio,
          outputContractItems = input.outputContractItems
        )

  def createTopLevel(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode
  ): Either[PieceDefinitionError, MovementDefinition] =
    createTopLevel(
      name = name,
      rules = rules,
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = executionMode,
      parallel = None,
      arpeggio = None,
      outputContractItems = OutputContractItems.Empty
    )

  def createTopLevel(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode,
      parallel: Option[ParallelConfiguration] = None,
      arpeggio: Option[ArpeggioConfiguration] = None,
      outputContractItems: OutputContractItems = OutputContractItems.Empty
  ): Either[PieceDefinitionError, MovementDefinition] =
    create(
      name = name,
      rules = rules,
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = executionMode,
      parallel = parallel,
      arpeggio = arpeggio,
      outputContractItems = outputContractItems,
      requireTransitionTarget = true
    )

  def createNested(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode
  ): Either[PieceDefinitionError, MovementDefinition] =
    createNested(
      name = name,
      rules = rules,
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = executionMode,
      parallel = None,
      arpeggio = None,
      outputContractItems = OutputContractItems.Empty
    )

  def createNested(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode,
      parallel: Option[ParallelConfiguration] = None,
      arpeggio: Option[ArpeggioConfiguration] = None,
      outputContractItems: OutputContractItems = OutputContractItems.Empty
  ): Either[PieceDefinitionError, MovementDefinition] =
    create(
      name = name,
      rules = rules,
      facets = facets,
      facetCatalog = facetCatalog,
      executionMode = executionMode,
      parallel = parallel,
      arpeggio = arpeggio,
      outputContractItems = outputContractItems,
      requireTransitionTarget = false
    )

  private def create(
      name: MovementName,
      rules: MovementRules,
      facets: MovementFacets,
      facetCatalog: FacetCatalog,
      executionMode: MovementExecutionMode,
      parallel: Option[ParallelConfiguration],
      arpeggio: Option[ArpeggioConfiguration],
      outputContractItems: OutputContractItems,
      requireTransitionTarget: Boolean
  ): Either[PieceDefinitionError, MovementDefinition] =
    for
      _ <- parseExecutionModeConfiguration(
        executionMode = executionMode,
        parallel = parallel,
        arpeggio = arpeggio
      )
      parsedRules <- parseRuleTransitionTargets(
        rules = rules,
        requireTransitionTarget = requireTransitionTarget
      )
      parsedFacetReferences <- facetCatalog.parseReferences(toFacetReferences(facets))
      parsedOutputContractItems <- OutputContractItems.create(
        outputContractItems.breachEncapsulationOfValues
      )
    yield MovementDefinition(
      name = name,
      rules = parsedRules,
      facets = toMovementFacets(parsedFacetReferences),
      executionMode = executionMode,
      parallel = resolvedParallelConfiguration(executionMode, parallel),
      arpeggio = resolvedArpeggioConfiguration(executionMode, arpeggio),
      outputContractItems = parsedOutputContractItems
    )

  private def parseRuleTransitionTargets(
      rules: MovementRules,
      requireTransitionTarget: Boolean
  ): Either[PieceDefinitionError, MovementRules] =
    if !requireTransitionTarget || rules.forall(_.next.nonEmpty) then Right(rules)
    else Left(PieceDefinitionError.MissingTopLevelRuleTransitionTarget)

  private def parseExecutionModeConfiguration(
      executionMode: MovementExecutionMode,
      parallel: Option[ParallelConfiguration],
      arpeggio: Option[ArpeggioConfiguration]
  ): Either[PieceDefinitionError, Unit] =
    executionMode match
      case MovementExecutionMode.Sequential =>
        if parallel.isEmpty && arpeggio.isEmpty then Right(())
        else Left(PieceDefinitionError.InvalidExecutionModeConfiguration)
      case MovementExecutionMode.Parallel =>
        if arpeggio.nonEmpty then Left(PieceDefinitionError.InvalidExecutionModeConfiguration)
        else Right(())
      case MovementExecutionMode.Arpeggio =>
        if parallel.nonEmpty then Left(PieceDefinitionError.InvalidExecutionModeConfiguration)
        else Right(())
      case MovementExecutionMode.TeamLeader(_) =>
        if parallel.isEmpty && arpeggio.isEmpty then Right(())
        else Left(PieceDefinitionError.InvalidExecutionModeConfiguration)

  private def resolvedParallelConfiguration(
      executionMode: MovementExecutionMode,
      parallel: Option[ParallelConfiguration]
  ): Option[ParallelConfiguration] =
    executionMode match
      case MovementExecutionMode.Parallel =>
        Some(parallel.getOrElse(ParallelConfiguration.Empty))
      case _ =>
        None

  private def resolvedArpeggioConfiguration(
      executionMode: MovementExecutionMode,
      arpeggio: Option[ArpeggioConfiguration]
  ): Option[ArpeggioConfiguration] =
    executionMode match
      case MovementExecutionMode.Arpeggio =>
        Some(arpeggio.getOrElse(ArpeggioConfiguration.Default))
      case _ =>
        None

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
