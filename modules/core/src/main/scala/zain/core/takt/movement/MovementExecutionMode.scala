package zain.core.takt.movement

enum MovementExecutionMode:
  case Sequential
  case Parallel
  case Arpeggio
  case TeamLeader(configuration: TeamLeaderConfiguration)
