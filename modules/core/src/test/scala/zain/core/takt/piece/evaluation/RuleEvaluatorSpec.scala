package zain.core.takt.piece.evaluation

import org.scalatest.funsuite.AnyFunSuite
import zain.core.takt.facet.FacetCatalog
import zain.core.takt.movement.MovementDefinition
import zain.core.takt.movement.MovementDefinitions
import zain.core.takt.movement.MovementExecutionMode
import zain.core.takt.movement.MovementFacets
import zain.core.takt.movement.MovementRule
import zain.core.takt.movement.MovementRules
import zain.core.takt.movement.ParallelConfiguration
import zain.core.takt.piece.MovementOutput
import zain.core.takt.piece.PieceDefinitionFactory
import zain.core.takt.piece.PieceDraft
import zain.core.takt.piece.PieceExecutionError
import zain.core.takt.piece.PieceExecutionState
import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.PieceName

final class RuleEvaluatorSpec extends AnyFunSuite:

  test("should evaluate aggregate condition before tag detection"):
    val reviewerA = createMovementDefinition(
      name = "review-a",
      rules = MovementRules.create(
        Vector(
          parseRule("approved", Some("COMPLETE")),
          parseRule("needs_fix", Some("ABORT"))
        )
      )
    )
    val reviewerB = createMovementDefinition(
      name = "review-b",
      rules = MovementRules.create(
        Vector(
          parseRule("approved", Some("COMPLETE")),
          parseRule("needs_fix", Some("ABORT"))
        )
      )
    )
    val parent = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("""all("approved")""", Some("COMPLETE")),
          parseRule("""any("needs_fix")""", Some("ABORT"))
        )
      ),
      executionMode = MovementExecutionMode.Parallel,
      parallel = Some(
        ParallelConfiguration.create(
          MovementDefinitions.create(Vector(reviewerA, reviewerB))
        )
      )
    )
    val state = createState(parent)
      .recordMovementOutput(parseMovementName("review-a"), parseMovementOutput(matchedRuleIndex = Some(0)))
      .recordMovementOutput(parseMovementName("review-b"), parseMovementOutput(matchedRuleIndex = Some(0)))
    val evaluator = new RuleEvaluator(
      movement = parent,
      context = RuleEvaluatorContext(
        state = state,
        interactive = false,
        detectRuleIndex = StaticRuleIndexDetector.fixed(Some(1)),
        aiConditionJudge = StaticAiConditionJudge.fixed(None)
      )
    )

    val actual = evaluator.evaluate(agentContent = "ignored", tagContent = "[REVIEW:2]")

    assert(actual == Right(Some(RuleMatch(index = 0, method = RuleMatchMethod.Aggregate))))

  test("should evaluate phase3 tag before phase1 tag"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("approved", Some("COMPLETE")),
          parseRule("needs_fix", Some("ABORT"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = false,
        detectRuleIndex = new RuleIndexDetector:
          override def detect(content: String, movementName: MovementName): Option[Int] =
            if content == "phase3" then Some(1) else Some(0),
        aiConditionJudge = StaticAiConditionJudge.fixed(None)
      )
    )

    val actual = evaluator.evaluate(agentContent = "phase1", tagContent = "phase3")

    assert(actual == Right(Some(RuleMatch(index = 1, method = RuleMatchMethod.Phase3Tag))))

  test("should fallback to phase1 tag when phase3 does not match"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("approved", Some("COMPLETE")),
          parseRule("needs_fix", Some("ABORT"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = false,
        detectRuleIndex = new RuleIndexDetector:
          override def detect(content: String, movementName: MovementName): Option[Int] =
            if content == "phase3" then None else Some(0),
        aiConditionJudge = StaticAiConditionJudge.fixed(None)
      )
    )

    val actual = evaluator.evaluate(agentContent = "phase1", tagContent = "phase3")

    assert(actual == Right(Some(RuleMatch(index = 0, method = RuleMatchMethod.Phase1Tag))))

  test("should evaluate ai conditions with ai judge"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("""ai("approved?")""", Some("COMPLETE")),
          parseRule("""ai("needs_fix?")""", Some("ABORT"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = true,
        detectRuleIndex = StaticRuleIndexDetector.fixed(None),
        aiConditionJudge = StaticAiConditionJudge.fixed(Some(1))
      )
    )

    val actual = evaluator.evaluate(agentContent = "agent output", tagContent = "")

    assert(actual == Right(Some(RuleMatch(index = 1, method = RuleMatchMethod.AiJudge))))

  test("should use ai fallback for plain conditions"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("approved", Some("COMPLETE")),
          parseRule("needs_fix", Some("ABORT"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = true,
        detectRuleIndex = StaticRuleIndexDetector.fixed(None),
        aiConditionJudge = StaticAiConditionJudge.fixed(Some(0))
      )
    )

    val actual = evaluator.evaluate(agentContent = "agent output", tagContent = "")

    assert(actual == Right(Some(RuleMatch(index = 0, method = RuleMatchMethod.AiJudgeFallback))))

  test("should skip interactiveOnly rule in non interactive mode"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("manual fix", Some("ABORT"), interactiveOnly = true),
          parseRule("auto fix", Some("COMPLETE"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = false,
        detectRuleIndex = StaticRuleIndexDetector.fixed(Some(0)),
        aiConditionJudge = StaticAiConditionJudge.fixed(Some(0))
      )
    )

    val actual = evaluator.evaluate(agentContent = "agent output", tagContent = "phase3")

    assert(actual == Right(Some(RuleMatch(index = 1, method = RuleMatchMethod.AiJudgeFallback))))

  test("should return rule not matched when no rule matches"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("approved", Some("COMPLETE")),
          parseRule("needs_fix", Some("ABORT"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = false,
        detectRuleIndex = StaticRuleIndexDetector.fixed(None),
        aiConditionJudge = StaticAiConditionJudge.fixed(None)
      )
    )

    val actual = evaluator.evaluate(agentContent = "", tagContent = "")

    assert(actual == Left(PieceExecutionError.RuleNotMatched(movement.name)))

  test("should reject out of bounds ai judge decision"):
    val movement = createMovementDefinition(
      name = "review",
      rules = MovementRules.create(
        Vector(
          parseRule("""ai("approved?")""", Some("COMPLETE")),
          parseRule("""ai("needs_fix?")""", Some("ABORT"))
        )
      )
    )
    val evaluator = new RuleEvaluator(
      movement = movement,
      context = RuleEvaluatorContext(
        state = createState(movement),
        interactive = true,
        detectRuleIndex = StaticRuleIndexDetector.fixed(None),
        aiConditionJudge = StaticAiConditionJudge.fixed(Some(9))
      )
    )

    val actual = evaluator.evaluate(agentContent = "agent output", tagContent = "")

    assert(actual == Left(PieceExecutionError.InvalidAiJudgeDecision(index = 9, conditionCount = 2)))

  private def createState(movement: MovementDefinition): PieceExecutionState =
    val piece = PieceDefinitionFactory.create(
      PieceDraft(
        name = parsePieceName("references-takt-always-valid"),
        movements = MovementDefinitions.create(Vector(movement)),
        initialMovement = Some(movement.name),
        maxMovements = Some(10)
      )
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"piece creation should succeed: $error")

    PieceExecutionState.start(piece)

  private def createMovementDefinition(
      name: String,
      rules: MovementRules,
      executionMode: MovementExecutionMode = MovementExecutionMode.Sequential,
      parallel: Option[ParallelConfiguration] = None
  ): MovementDefinition =
    MovementDefinition.createTopLevel(
      name = parseMovementName(name),
      rules = rules,
      facets = MovementFacets.Empty,
      facetCatalog = FacetCatalog.Empty,
      executionMode = executionMode,
      parallel = parallel
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement creation should succeed: $error")

  private def parseRule(
      condition: String,
      next: Option[String],
      interactiveOnly: Boolean = false
  ): MovementRule =
    MovementRule.create(
      condition = condition,
      next = next,
      appendix = None,
      requiresUserInput = false,
      interactiveOnly = interactiveOnly
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"rule parsing should succeed: $error")

  private def parseMovementOutput(matchedRuleIndex: Option[Int]): MovementOutput =
    MovementOutput.create(
      content = "",
      matchedRuleIndex = matchedRuleIndex
    ) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement output parsing should succeed: $error")

  private def parseMovementName(value: String): MovementName =
    MovementName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"movement name parsing should succeed: $error")

  private def parsePieceName(value: String): PieceName =
    PieceName.parse(value) match
      case Right(parsed) => parsed
      case Left(error)   => fail(s"piece name parsing should succeed: $error")

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
