package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError
import zain.core.takt.primitives.PartPermissionMode
import zain.core.takt.primitives.TeamLeaderMaxParts
import zain.core.takt.primitives.TeamLeaderTimeoutMillis

final case class TeamLeaderConfiguration private (
    maxParts: TeamLeaderMaxParts,
    timeoutMillis: TeamLeaderTimeoutMillis,
    persona: Option[String],
    partPersona: Option[String],
    partAllowedTools: Vector[String],
    partEdit: Option[Boolean],
    partPermissionMode: Option[PartPermissionMode]
)

object TeamLeaderConfiguration:
  def create(
      maxParts: Int,
      timeoutMillis: Int,
      persona: Option[String] = None,
      partPersona: Option[String] = None,
      partAllowedTools: Vector[String] = Vector.empty,
      partEdit: Option[Boolean] = None,
      partPermissionMode: Option[String] = None
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
      parsedPartPermissionMode <- parsePartPermissionMode(partPermissionMode)
    yield TeamLeaderConfiguration(
      maxParts = parsedMaxParts,
      timeoutMillis = parsedTimeoutMillis,
      persona = persona,
      partPersona = partPersona,
      partAllowedTools = partAllowedTools,
      partEdit = partEdit,
      partPermissionMode = parsedPartPermissionMode
    )

  private def parsePartPermissionMode(
      value: Option[String]
  ): Either[PieceDefinitionError, Option[PartPermissionMode]] =
    value match
      case None => Right(None)
      case Some(raw) =>
        PartPermissionMode
          .parse(raw)
          .left
          .map(_ => PieceDefinitionError.InvalidPartPermissionMode(raw))
          .map(Some(_))
