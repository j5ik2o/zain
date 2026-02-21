package zain.core.takt.primitives

enum TaktPrimitiveError:
  case EmptyFacetName
  case EmptyMovementIdentifier
  case EmptyPieceName
  case EmptyPartId
  case EmptyPartTitle
  case EmptyPartInstruction
  case EmptyPersonaName
  case EmptyPersonaSessionId
  case EmptyUserInput
  case InvalidPartPermissionMode(value: String)
  case EmptyRuleCondition
  case InvalidRuleConditionSyntax
  case EmptyTransitionTarget
  case NonPositivePartTimeoutMillis
  case NonPositiveTeamLeaderMaxParts
  case TeamLeaderMaxPartsExceedsLimit
  case NonPositiveTeamLeaderTimeoutMillis
  case NonPositiveMaxMovements
  case NegativeIterationCount
