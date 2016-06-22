package performanceanalysis.server.messages

import scala.concurrent.duration.Duration

object Rules {

  /**
    * Encapsulates a basic alerting rule.
    */
  case class AlertRule(threshold: Threshold, action: Action)

  /** Defines threshold of a rule. */
  case class Threshold(max: String) {
    def limit: Duration = Duration(max)
  }

  case class Action(url: String)

}

