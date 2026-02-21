package zain.core.takt.facet

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.FacetName

final case class FacetCatalog private (
    personas: Set[FacetName],
    policies: Set[FacetName],
    knowledge: Set[FacetName],
    instructions: Set[FacetName],
    outputContracts: Set[FacetName]
):
  def parseReferences(facets: FacetReferences): Either[PieceDefinitionError, FacetReferences] =
    facets.entries
      .foldLeft[Either[PieceDefinitionError, FacetReferenceEntries]](Right(FacetReferenceEntries.Empty)) { (acc, entry) =>
        for
          parsedEntries <- acc
          parsedEntry <- parseReferenceEntry(entry)
        yield parsedEntries ++ FacetReferenceEntries.one(parsedEntry)
      }
      .map(_ => facets)

  private def parseReferenceEntry(
      entry: FacetReferenceEntry
  ): Either[PieceDefinitionError, FacetReferenceEntry] =
    val allowedReferences = allowedReferencesOf(entry.category)

    if allowedReferences.contains(entry.reference) then Right(entry)
    else Left(undefinedReferenceErrorOf(entry))

  private def allowedReferencesOf(category: FacetCategory): Set[FacetName] =
    category match
      case FacetCategory.Persona        => personas
      case FacetCategory.Policy         => policies
      case FacetCategory.Knowledge      => knowledge
      case FacetCategory.Instruction    => instructions
      case FacetCategory.OutputContract => outputContracts

  private def undefinedReferenceErrorOf(entry: FacetReferenceEntry): PieceDefinitionError =
    entry.category match
      case FacetCategory.Persona        => PieceDefinitionError.UndefinedPersonaReference(entry.reference)
      case FacetCategory.Policy         => PieceDefinitionError.UndefinedPolicyReference(entry.reference)
      case FacetCategory.Knowledge      => PieceDefinitionError.UndefinedKnowledgeReference(entry.reference)
      case FacetCategory.Instruction    => PieceDefinitionError.UndefinedInstructionReference(entry.reference)
      case FacetCategory.OutputContract => PieceDefinitionError.UndefinedOutputContractReference(entry.reference)

object FacetCatalog:
  val Empty: FacetCatalog = FacetCatalog(
    personas = Set.empty,
    policies = Set.empty,
    knowledge = Set.empty,
    instructions = Set.empty,
    outputContracts = Set.empty
  )

  def create(
      personas: FacetNames,
      policies: FacetNames,
      knowledge: FacetNames,
      instructions: FacetNames,
      outputContracts: FacetNames
  ): FacetCatalog =
    FacetCatalog(
      personas = personas.toSet,
      policies = policies.toSet,
      knowledge = knowledge.toSet,
      instructions = instructions.toSet,
      outputContracts = outputContracts.toSet
    )

  def fromDefinitions(
      personas: FacetNames,
      policies: FacetNames,
      knowledge: FacetNames,
      instructions: FacetNames,
      outputContracts: FacetNames
  ): FacetCatalog =
    create(
      personas = personas,
      policies = policies,
      knowledge = knowledge,
      instructions = instructions,
      outputContracts = outputContracts
    )
