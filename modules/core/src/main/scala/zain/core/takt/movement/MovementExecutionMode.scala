package zain.core.takt.movement

import zain.core.takt.piece.PieceDefinitionError

enum MovementExecutionMode:
  case Sequential
  case Parallel
  case Arpeggio
  case TeamLeader(configuration: TeamLeaderConfiguration)

object MovementExecutionMode:
  def resolve(
      hasParallel: Boolean,
      hasArpeggio: Boolean,
      teamLeader: Option[TeamLeaderConfiguration]
  ): Either[PieceDefinitionError, MovementExecutionMode] =
    val enabledCount = Vector(hasParallel, hasArpeggio, teamLeader.nonEmpty).count(identity)

    if enabledCount > 1 then Left(PieceDefinitionError.ConflictingExecutionModes)
    else if hasParallel then Right(MovementExecutionMode.Parallel)
    else if hasArpeggio then Right(MovementExecutionMode.Arpeggio)
    else
      teamLeader match
        case Some(configuration) => Right(MovementExecutionMode.TeamLeader(configuration))
        case None                => Right(MovementExecutionMode.Sequential)
