package performanceanalysis.server

import performanceanalysis.server.messages.AdministratorMessages.{RegisterComponent, RegisteredComponents}
import performanceanalysis.server.messages.AlertMessages.AllAlertRuleDetails
import performanceanalysis.server.messages.LogMessages.{Details, Metric}
import performanceanalysis.server.messages.Rules
import spray.json.DefaultJsonProtocol

object Protocol {

}

trait Protocol extends DefaultJsonProtocol {
  implicit val metricFormatter = jsonFormat(Metric.apply, "metric-key", "regex")
  implicit val detailsFormatter = jsonFormat1(Details.apply)
  implicit val registerComponentsFormatter = jsonFormat1(RegisterComponent.apply)
  implicit val registeredComponentsFormatter = jsonFormat1(RegisteredComponents.apply)

  implicit val thresholdRuleFormatter = jsonFormat1(Rules.Threshold.apply)
  implicit val actionRuleFormatter = jsonFormat1(Rules.Action.apply)
  implicit val alertingRuleFormatter = jsonFormat2(Rules.AlertRule.apply)

  implicit val alertRulesDetailsFormatter = jsonFormat1(AllAlertRuleDetails.apply)
}
