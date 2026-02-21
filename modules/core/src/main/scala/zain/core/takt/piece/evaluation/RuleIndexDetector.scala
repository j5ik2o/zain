package zain.core.takt.piece.evaluation

import zain.core.takt.primitives.MovementName
import zain.core.takt.primitives.RuleDetectionContent

trait RuleIndexDetector:
  def detect(content: RuleDetectionContent, movementName: MovementName): Option[Int]
