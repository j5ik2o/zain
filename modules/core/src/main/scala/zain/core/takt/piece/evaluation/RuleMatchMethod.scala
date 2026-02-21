package zain.core.takt.piece.evaluation

enum RuleMatchMethod:
  case Aggregate
  case AutoSelect
  case StructuredOutput
  case Phase3Tag
  case Phase1Tag
  case AiJudge
  case AiJudgeFallback
