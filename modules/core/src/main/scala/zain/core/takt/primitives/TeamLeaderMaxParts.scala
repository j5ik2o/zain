package zain.core.takt.primitives

final case class TeamLeaderMaxParts private (value: Int)

object TeamLeaderMaxParts:
  val UpperLimit: Int = 3

  def parse(value: Int): Either[TaktPrimitiveError, TeamLeaderMaxParts] =
    if value <= 0 then Left(TaktPrimitiveError.NonPositiveTeamLeaderMaxParts)
    else if value > UpperLimit then Left(TaktPrimitiveError.TeamLeaderMaxPartsExceedsLimit)
    else Right(TeamLeaderMaxParts(value))
