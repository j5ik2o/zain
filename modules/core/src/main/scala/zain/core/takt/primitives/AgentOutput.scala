package zain.core.takt.primitives

final case class AgentOutput private (value: String):
  def isEmpty: Boolean =
    value.isEmpty

object AgentOutput:
  def parse(value: String): Either[TaktPrimitiveError, AgentOutput] =
    Right(AgentOutput(value))

  def from(value: String): AgentOutput =
    AgentOutput(value)
