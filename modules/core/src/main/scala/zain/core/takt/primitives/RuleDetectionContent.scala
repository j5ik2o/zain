package zain.core.takt.primitives

final case class RuleDetectionContent private (value: String):
  def isEmpty: Boolean =
    value.isEmpty

object RuleDetectionContent:
  def parse(value: String): Either[TaktPrimitiveError, RuleDetectionContent] =
    Right(RuleDetectionContent(value))

  def from(value: String): RuleDetectionContent =
    RuleDetectionContent(value)
