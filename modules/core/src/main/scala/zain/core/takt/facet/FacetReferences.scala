package zain.core.takt.facet

import zain.core.takt.primitives.FacetName

final case class FacetReferences private (
    persona: Option[FacetName],
    policies: FacetNames,
    knowledge: FacetNames,
    instruction: Option[FacetName],
    outputContracts: FacetNames
):
  def entries: FacetReferenceEntries =
    personaEntries ++
      policyEntries ++
      knowledgeEntries ++
      instructionEntries ++
      outputContractEntries

  private def personaEntries: FacetReferenceEntries =
    persona match
      case Some(value) =>
        FacetReferenceEntries.one(FacetReferenceEntry.of(FacetCategory.Persona, value))
      case None =>
        FacetReferenceEntries.Empty

  private def policyEntries: FacetReferenceEntries =
    FacetReferenceEntries.create(
      policies.map(FacetReferenceEntry.of(FacetCategory.Policy, _))
    )

  private def knowledgeEntries: FacetReferenceEntries =
    FacetReferenceEntries.create(
      knowledge.map(FacetReferenceEntry.of(FacetCategory.Knowledge, _))
    )

  private def instructionEntries: FacetReferenceEntries =
    instruction match
      case Some(value) =>
        FacetReferenceEntries.one(FacetReferenceEntry.of(FacetCategory.Instruction, value))
      case None =>
        FacetReferenceEntries.Empty

  private def outputContractEntries: FacetReferenceEntries =
    FacetReferenceEntries.create(
      outputContracts.map(FacetReferenceEntry.of(FacetCategory.OutputContract, _))
    )

object FacetReferences:
  def create(
      persona: Option[FacetName],
      policies: FacetNames,
      knowledge: FacetNames,
      instruction: Option[FacetName],
      outputContracts: FacetNames
  ): FacetReferences =
    FacetReferences(
      persona = persona,
      policies = policies,
      knowledge = knowledge,
      instruction = instruction,
      outputContracts = outputContracts
    )
