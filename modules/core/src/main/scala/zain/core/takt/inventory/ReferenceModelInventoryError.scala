package zain.core.takt.inventory

enum ReferenceModelInventoryError:
  case MissingReferenceSources(target: ReferenceModelTarget)
  case EmptyReferenceSourcePath(target: ReferenceModelTarget)
  case MissingExclusionReason(excludedName: String)
