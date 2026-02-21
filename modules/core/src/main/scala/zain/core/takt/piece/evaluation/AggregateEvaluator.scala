package zain.core.takt.piece.evaluation

import zain.core.takt.movement.MovementDefinition
import zain.core.takt.primitives.RuleCondition
import zain.core.takt.piece.PieceExecutionState

final class AggregateEvaluator(
    movement: MovementDefinition,
    state: PieceExecutionState
):
  def evaluate(): Option[Int] =
    val parallelSubMovements = movement.parallel
      .map(_.subMovements.breachEncapsulationOfValues)
      .getOrElse(Vector.empty)

    if movement.rules.isEmpty || parallelSubMovements.isEmpty then None
    else
      movement.rules.indices.collectFirst {
        case index if ruleAt(index).exists(rule =>
              matchesAggregateRule(
                ruleCondition = rule.condition,
                parallelSubMovements = parallelSubMovements
              )
            ) =>
          index
      }

  private def ruleAt(index: Int) =
    movement.rules.ruleAt(index)

  private def matchesAggregateRule(
      ruleCondition: RuleCondition,
      parallelSubMovements: Vector[MovementDefinition]
  ): Boolean =
    ruleCondition match
      case RuleCondition.Aggregate(aggregateType, conditions) =>
        matchesAggregateCondition(
          aggregateType = aggregateType,
          expectedConditions = conditions.map(_.value),
          parallelSubMovements = parallelSubMovements
        )
      case _ =>
        false

  private def matchesAggregateCondition(
      aggregateType: RuleCondition.AggregateType,
      expectedConditions: Vector[String],
      parallelSubMovements: Vector[MovementDefinition]
  ): Boolean =
    aggregateType match
      case RuleCondition.AggregateType.All =>
        matchesAllConditions(
          expectedConditions = expectedConditions,
          parallelSubMovements = parallelSubMovements
        )
      case RuleCondition.AggregateType.Any =>
        matchesAnyConditions(
          expectedConditions = expectedConditions,
          parallelSubMovements = parallelSubMovements
        )

  private def matchesAllConditions(
      expectedConditions: Vector[String],
      parallelSubMovements: Vector[MovementDefinition]
  ): Boolean =
    if expectedConditions.size == 1 then
      val expectedCondition = expectedConditions.head
      parallelSubMovements.forall: subMovement =>
        matchedConditionTextOf(subMovement).contains(expectedCondition)
    else if expectedConditions.size != parallelSubMovements.size then false
    else
      parallelSubMovements.zip(expectedConditions).forall {
        case (subMovement, expectedCondition) =>
          matchedConditionTextOf(subMovement).contains(expectedCondition)
      }

  private def matchesAnyConditions(
      expectedConditions: Vector[String],
      parallelSubMovements: Vector[MovementDefinition]
  ): Boolean =
    val expectedSet = expectedConditions.toSet

    parallelSubMovements.exists: subMovement =>
      matchedConditionTextOf(subMovement).exists(expectedSet.contains)

  private def matchedConditionTextOf(subMovement: MovementDefinition): Option[String] =
    state.matchedRuleIndexOf(subMovement.name).flatMap: matchedRuleIndex =>
      subMovement.rules.ruleAt(matchedRuleIndex).map(_.condition.breachEncapsulationOfRawValue)
