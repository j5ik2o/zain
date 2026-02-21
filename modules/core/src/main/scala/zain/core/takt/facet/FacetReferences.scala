package zain.core.takt.facet

import zain.core.takt.primitives.FacetName

final case class FacetReferences private (
    persona: Option[FacetName],
    policies: Vector[FacetName],
    knowledge: Vector[FacetName],
    instruction: Option[FacetName],
    outputContracts: Vector[FacetName]
):
  def entries: Vector[FacetReferenceEntry] =
    personaEntries ++
      policyEntries ++
      knowledgeEntries ++
      instructionEntries ++
      outputContractEntries

  private def personaEntries: Vector[FacetReferenceEntry] =
    persona.toVector.map(FacetReferenceEntry.of(FacetCategory.Persona, _))

  private def policyEntries: Vector[FacetReferenceEntry] =
    policies.map(FacetReferenceEntry.of(FacetCategory.Policy, _))

  private def knowledgeEntries: Vector[FacetReferenceEntry] =
    knowledge.map(FacetReferenceEntry.of(FacetCategory.Knowledge, _))

  private def instructionEntries: Vector[FacetReferenceEntry] =
    instruction.toVector.map(FacetReferenceEntry.of(FacetCategory.Instruction, _))

  private def outputContractEntries: Vector[FacetReferenceEntry] =
    outputContracts.map(FacetReferenceEntry.of(FacetCategory.OutputContract, _))

object FacetReferences:
  def create(
      persona: Option[FacetName],
      policies: Vector[FacetName],
      knowledge: Vector[FacetName],
      instruction: Option[FacetName],
      outputContracts: Vector[FacetName]
  ): FacetReferences =
    FacetReferences(
      persona = persona,
      policies = policies,
      knowledge = knowledge,
      instruction = instruction,
      outputContracts = outputContracts
    )
