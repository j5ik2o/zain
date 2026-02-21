package zain.core.takt.primitives

object TaktPrimitiveParsers:
  def parseFacetName(value: String): Either[TaktPrimitiveError, FacetName] =
    FacetName.parse(value)

  def parseMovementIdentifier(value: String): Either[TaktPrimitiveError, MovementName] =
    MovementName.parse(value)

  def parsePieceName(value: String): Either[TaktPrimitiveError, PieceName] =
    PieceName.parse(value)

  def parsePartId(value: String): Either[TaktPrimitiveError, PartId] =
    PartId.parse(value)

  def parsePartTitle(value: String): Either[TaktPrimitiveError, PartTitle] =
    PartTitle.parse(value)

  def parsePartInstruction(value: String): Either[TaktPrimitiveError, PartInstruction] =
    PartInstruction.parse(value)

  def parsePartPermissionMode(value: String): Either[TaktPrimitiveError, PartPermissionMode] =
    PartPermissionMode.parse(value)

  def parsePartTimeoutMillis(value: Int): Either[TaktPrimitiveError, PartTimeoutMillis] =
    PartTimeoutMillis.parse(value)

  def parseTeamLeaderMaxParts(value: Int): Either[TaktPrimitiveError, TeamLeaderMaxParts] =
    TeamLeaderMaxParts.parse(value)

  def parseTeamLeaderTimeoutMillis(value: Int): Either[TaktPrimitiveError, TeamLeaderTimeoutMillis] =
    TeamLeaderTimeoutMillis.parse(value)

  def parseRuleCondition(value: String): Either[TaktPrimitiveError, RuleCondition] =
    RuleCondition.parse(value)

  def parseTransitionTarget(value: String): Either[TaktPrimitiveError, TransitionTarget] =
    TransitionTarget.parse(value)

  def parseMaxMovements(value: Int): Either[TaktPrimitiveError, MaxMovements] =
    MaxMovements.parse(value)

  def parseIterationCount(value: Int): Either[TaktPrimitiveError, IterationCount] =
    IterationCount.parse(value)
