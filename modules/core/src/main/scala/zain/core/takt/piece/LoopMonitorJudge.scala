package zain.core.takt.piece

final case class LoopMonitorJudge(
    persona: Option[String],
    instructionTemplate: Option[String],
    rules: LoopMonitorRules
)
