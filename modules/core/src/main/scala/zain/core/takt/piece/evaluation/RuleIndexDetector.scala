package zain.core.takt.piece.evaluation

import zain.core.takt.primitives.MovementName

trait RuleIndexDetector:
  def detect(content: String, movementName: MovementName): Option[Int]
