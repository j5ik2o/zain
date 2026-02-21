package zain.core.takt.piece

import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.TransitionTarget

final case class LoopMonitorConfigurations private (
    private val values: Vector[LoopMonitorConfiguration]
):
  def parseDefinedMovements(
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, LoopMonitorConfigurations] =
    values
      .foldLeft[Either[PieceDefinitionError, Unit]](Right(())) { (acc, monitor) =>
        acc.flatMap(_ => LoopMonitorConfigurations.parseCycleMovements(monitor, movementNames))
          .flatMap(_ => LoopMonitorConfigurations.parseJudgeRuleTargets(monitor, movementNames))
      }
      .map(_ => this)

  def breachEncapsulationOfValues: Vector[LoopMonitorConfiguration] =
    values

object LoopMonitorConfigurations:
  val Empty: LoopMonitorConfigurations = LoopMonitorConfigurations(Vector.empty)

  def create(values: Vector[LoopMonitorConfiguration]): LoopMonitorConfigurations =
    LoopMonitorConfigurations(values)

  private def parseCycleMovements(
      monitor: LoopMonitorConfiguration,
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, Unit] =
    monitor.cycle
      .find(movementName => !movementNames.contains(movementName))
      .map(PieceDefinitionError.UndefinedLoopMonitorCycleMovement.apply)
      .toLeft(())

  private def parseJudgeRuleTargets(
      monitor: LoopMonitorConfiguration,
      movementNames: Set[MovementName]
  ): Either[PieceDefinitionError, Unit] =
    monitor.judge.rules
      .breachEncapsulationOfValues
      .collectFirst:
        case rule
            if isUndefinedJudgeTarget(
              target = rule.next,
              movementNames = movementNames
            ) =>
          rule.next
      .flatMap:
        case TransitionTarget.Movement(name) => Some(name)
        case _                               => None
      .map(PieceDefinitionError.UndefinedLoopMonitorJudgeTarget.apply)
      .toLeft(())

  private def isUndefinedJudgeTarget(
      target: TransitionTarget,
      movementNames: Set[MovementName]
  ): Boolean =
    target match
      case TransitionTarget.Complete => false
      case TransitionTarget.Abort    => false
      case TransitionTarget.Movement(name) =>
        !movementNames.contains(name)
