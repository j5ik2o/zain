package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.TeamLeaderMaxParts
import zain.core.takt.primitives.TeamLeaderTimeoutMillis

final case class TeamLeaderConfiguration private (
    maxParts: TeamLeaderMaxParts,
    timeoutMillis: TeamLeaderTimeoutMillis
)

object TeamLeaderConfiguration:
  def create(
      maxParts: Int,
      timeoutMillis: Int
  ): Either[PieceDefinitionError, TeamLeaderConfiguration] =
    for
      parsedMaxParts <- TeamLeaderMaxParts
        .parse(maxParts)
        .left
        .map(_ => PieceDefinitionError.TeamLeaderMaxPartsOutOfRange)
      parsedTimeoutMillis <- TeamLeaderTimeoutMillis
        .parse(timeoutMillis)
        .left
        .map(_ => PieceDefinitionError.NonPositiveTeamLeaderTimeoutMillis)
    yield TeamLeaderConfiguration(
      maxParts = parsedMaxParts,
      timeoutMillis = parsedTimeoutMillis
    )
