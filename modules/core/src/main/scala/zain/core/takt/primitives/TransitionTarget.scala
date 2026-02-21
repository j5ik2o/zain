package zain.core.takt.primitives

enum TransitionTarget:
  case Complete
  case Abort
  case Movement(name: MovementName)

object TransitionTarget:
  private val CompleteKeyword = "COMPLETE"
  private val AbortKeyword = "ABORT"

  def parse(value: String): Either[TaktPrimitiveError, TransitionTarget] =
    if value.isEmpty then Left(TaktPrimitiveError.EmptyTransitionTarget)
    else
      value match
        case CompleteKeyword => Right(TransitionTarget.Complete)
        case AbortKeyword    => Right(TransitionTarget.Abort)
        case movement        => MovementName.parse(movement).map(TransitionTarget.Movement.apply)
