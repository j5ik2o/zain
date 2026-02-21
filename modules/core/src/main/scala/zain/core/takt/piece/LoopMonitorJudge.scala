package zain.core.takt.piece

import zain.core.takt.primitives.PersonaName

final case class LoopMonitorJudge(
    persona: Option[PersonaName],
    instructionTemplate: Option[LoopMonitorInstructionTemplate],
    rules: LoopMonitorRules
)
