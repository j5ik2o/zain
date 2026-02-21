package zain.core.takt.piece

import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.FacetName

enum PieceDefinitionError:
  case EmptyMovements
  case DuplicateMovementName(name: MovementName)
  case InitialMovementNotFound(initialMovement: MovementName)
  case UndefinedTransitionTarget(target: MovementName, from: MovementName)
  case NonPositiveMaxMovements
  case EmptyRuleCondition
  case InvalidRuleCondition
  case EmptyRuleAppendix
  case EmptyRuleTransitionTarget
  case MissingTopLevelRuleTransitionTarget
  case EmptyOutputContractName
  case EmptyOutputContractFormat
  case EmptyOutputContractOrder
  case DuplicateOutputContractItemName(name: String)
  case EmptyPartId
  case EmptyPartTitle
  case EmptyPartInstruction
  case InvalidPartPermissionMode(value: String)
  case NonPositivePartTimeoutMillis
  case TeamLeaderMaxPartsOutOfRange
  case NonPositiveTeamLeaderTimeoutMillis
  case NonPositiveArpeggioBatchSize
  case NonPositiveArpeggioConcurrency
  case InvalidExecutionModeConfiguration
  case NonPositiveLoopDetectionMaxConsecutiveSameStep
  case EmptyLoopMonitorCycle
  case LoopMonitorCycleRequiresAtLeastTwoMovements
  case NonPositiveLoopMonitorThreshold
  case EmptyLoopMonitorJudgeRules
  case EmptyLoopMonitorRuleCondition
  case EmptyLoopMonitorRuleTransitionTarget
  case UndefinedLoopMonitorCycleMovement(movement: MovementName)
  case UndefinedLoopMonitorJudgeTarget(target: MovementName)
  case UndefinedPersonaReference(reference: FacetName)
  case UndefinedPolicyReference(reference: FacetName)
  case UndefinedKnowledgeReference(reference: FacetName)
  case UndefinedInstructionReference(reference: FacetName)
  case UndefinedOutputContractReference(reference: FacetName)
