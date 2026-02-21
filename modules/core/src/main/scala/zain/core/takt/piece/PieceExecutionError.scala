package zain.core.takt.piece

import zain.core.takt.primitives.MovementName

enum PieceExecutionError:
  case UndefinedTransitionMovement(target: MovementName)
  case AlreadyFinished(currentStatus: PieceExecutionStatus)
  case InvalidRuleIndex(index: Int)
  case RuleWithoutTransitionTarget(index: Int)
  case RuleNotMatched(movement: MovementName)
  case NegativeMatchedRuleIndex(index: Int)
  case InvalidAiJudgeDecision(index: Int, conditionCount: Int)
