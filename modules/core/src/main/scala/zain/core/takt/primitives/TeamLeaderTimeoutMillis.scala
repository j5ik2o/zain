package zain.core.takt.primitives

final case class TeamLeaderTimeoutMillis private (value: Int)

object TeamLeaderTimeoutMillis:
  def parse(value: Int): Either[TaktPrimitiveError, TeamLeaderTimeoutMillis] =
    if value <= 0 then Left(TaktPrimitiveError.NonPositiveTeamLeaderTimeoutMillis)
    else Right(TeamLeaderTimeoutMillis(value))
