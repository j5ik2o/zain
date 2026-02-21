package zain.core.takt.piece

final case class LoopMonitorRules private (
    private val values: Vector[LoopMonitorRule]
):
  def map[B](transform: LoopMonitorRule => B): Vector[B] =
    values.map(transform)

  def exists(predicate: LoopMonitorRule => Boolean): Boolean =
    values.exists(predicate)

  def breachEncapsulationOfValues: Vector[LoopMonitorRule] =
    values

object LoopMonitorRules:
  def parse(values: Vector[LoopMonitorRule]): Either[PieceDefinitionError, LoopMonitorRules] =
    if values.isEmpty then Left(PieceDefinitionError.EmptyLoopMonitorJudgeRules)
    else Right(LoopMonitorRules(values))

  def create(values: Vector[LoopMonitorRule]): Either[PieceDefinitionError, LoopMonitorRules] =
    parse(values)
