package performanceanalysis.server.messages

import performanceanalysis.rules.ExpressionParser
import performanceanalysis.rules.ExpressionParser.Expression

import scala.concurrent.duration.Duration
import scala.util.Try

object Rules {

  /**
    * Encapsulates a basic alerting rule.
    */
  case class AlertRule(when: String, action: Action) {
    def expression: Try[Expression] = new ExpressionParser {}.parse(when)
  }

  /** Defines threshold of a rule. */
  case class Threshold(max: String) {
    def limit: Duration = Duration(max)
  }

  case class Action(url: String)

}

