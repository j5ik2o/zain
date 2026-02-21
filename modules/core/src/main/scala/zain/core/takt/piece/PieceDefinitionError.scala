package zain.core.takt.piece

import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.FacetName

enum PieceDefinitionError:
  case EmptyMovements
  case DuplicateMovementName(name: MovementName)
  case InitialMovementNotFound(initialMovement: MovementName)
  case UndefinedTransitionTarget(target: MovementName, from: MovementName)
  case NonPositiveMaxMovements
  case ConflictingExecutionModes
  case EmptyRuleCondition
  case EmptyRuleTransitionTarget
  case MissingTopLevelRuleTransitionTarget
  case EmptyPartId
  case EmptyPartTitle
  case EmptyPartInstruction
  case NonPositivePartTimeoutMillis
  case TeamLeaderMaxPartsOutOfRange
  case NonPositiveTeamLeaderTimeoutMillis
  case UndefinedPersonaReference(reference: FacetName)
  case UndefinedPolicyReference(reference: FacetName)
  case UndefinedKnowledgeReference(reference: FacetName)
  case UndefinedInstructionReference(reference: FacetName)
  case UndefinedOutputContractReference(reference: FacetName)
