package zain.core.takt.piece

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.facet.FacetCatalog
import zain.core.takt.movement.MovementDefinition
import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.movement.MovementExecutionMode
import zain.core.takt.movement.MovementFacets
import zain.core.takt.movement.MovementRule
import zain.core.takt.movement.MovementRules
import zain.core.takt.piece.evaluation.AiConditionJudge
import zain.core.takt.piece.evaluation.RuleIndexDetector
import zain.core.takt.piece.evaluation.RuleJudgeCondition
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final class PieceExecutionSpec extends AnyFunSuite:
  private val planMovementName = parseMovementName("plan")
  private val implementMovementName = parseMovementName("implement")

  test("should expose current movement definition from piece execution"):
    val piece = createPieceDefinition()

    val actual = PieceExecution.start(piece).currentMovementDefinition

    assert(actual.exists(_.name == planMovementName))

  test("should evaluate and advance by domain logic only"):
    val piece = createPieceDefinition()
    val execution = PieceExecution.start(piece)

    val actual = execution.evaluateAndAdvance(
      agentContent = "done",
      tagContent = "[PLAN:1]",
      interactive = false,
      detectRuleIndex = StaticRuleIndexDetector.fixed(Some(0)),
      aiConditionJudge = StaticAiConditionJudge.fixed(None)
    )

    assert(actual.exists(_.state.currentMovement == implementMovementName))
    assert(actual.exists(_.state.iteration.value == 1))
    assert(actual.exists(_.state.movementIterations.get(planMovementName).exists(_.value == 1)))
    assert(actual.exists(_.state.matchedRuleIndexOf(planMovementName).contains(0)))

  test("should return rule not matched when evaluator cannot find a rule"):
    val piece = createPieceDefinition()
    val execution = PieceExecution.start(piece)

    val actual = execution.evaluateAndAdvance(
      agentContent = "",
      tagContent = "",
      interactive = false,
      detectRuleIndex = StaticRuleIndexDetector.fixed(None),
      aiConditionJudge = StaticAiConditionJudge.fixed(None)
    )

    assert(actual == Left(PieceExecutionError.RuleNotMatched(planMovementName)))

  private def createPieceDefinition(): PieceDefinition =
    val planMovement = createMovement("plan", next = "implement")
    val implementMovement = createMovement("implement", next = "COMPLETE")

    PieceDefinitionFactory.create(
      PieceDraft(
        name = parsePieceName("references-takt-always-valid"),
        movements = MovementDefinitions.create(Vector(planMovement, implementMovement)),
        initialMovement = Some(planMovementName),
        maxMovements = Some(10)
      )
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"piece creation should succeed: $error")

  private def createMovement(name: String, next: String): MovementDefinition =
    MovementDefinition.createTopLevel(
      name = parseMovementName(name),
      rules = MovementRules.create(Vector(parseRule(condition = "ok", next = Some(next)))),
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      executionMode = MovementExecutionMode.Sequential
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement creation should succeed: $error")

  private def parsePieceName(value: String): PieceName =
    PieceName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"piece name parsing should succeed: $error")

  private def parseMovementName(value: String): MovementName =
    MovementName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement name parsing should succeed: $error")

  private def parseRule(condition: String, next: Option[String]): MovementRule =
    MovementRule.create(condition, next) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"rule parsing should succeed: $error")

  private final class StaticRuleIndexDetector(
      fixedValue: Option[Int]
  ) extends RuleIndexDetector:
    override def detect(content: String, movementName: MovementName): Option[Int] =
      fixedValue

  private object StaticRuleIndexDetector:
    def fixed(value: Option[Int]): RuleIndexDetector =
      new StaticRuleIndexDetector(value)

  private final class StaticAiConditionJudge(
      fixedValue: Option[Int]
  ) extends AiConditionJudge:
    override def judge(
        agentOutput: String,
        conditions: Vector[RuleJudgeCondition]
    ): Either[PieceExecutionError, Option[Int]] =
      Right(fixedValue)

  private object StaticAiConditionJudge:
    def fixed(value: Option[Int]): AiConditionJudge =
      new StaticAiConditionJudge(value)
