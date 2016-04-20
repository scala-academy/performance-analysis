package performanceanalysis.server

import akka.actor.ActorRef
import performanceanalysis.server.Protocol.Rules.AlertingRule
import performanceanalysis.server.Protocol._
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration.Duration

object Protocol {

  /**
    * Used by Administrator towards AdministratorActor to register a new component
    */
  case class RegisterComponent(componentId: String)

  /**
    * Used by AdministratorActor towards Administrator to signal that a new component was registered
    */
  case class LogParserCreated(componentId: String)

  /**
    * Used by AdministratorActor towards Administrator to signal that the component was already registered
    */
  case class LogParserExisted(componentId: String)

  /**
    * Used by AdministratorActor towards Administrator and by LogReceiverActor towards LogReceiver
    * to signal that the component could not be found
    */
  case class LogParserNotFound(componentId: String)

  /**
    * Used by Administrator towards AdministratorActor to request details of a component
    */
  case class GetDetails(componentId: String)

  /**
    * Used by Administrator towards AdministratorActor to request a list of all registered components
    */
  case object GetRegisteredComponents

  /**
    * Used by AdministratorActor towards Administrator to return a list of all registered components
    */
  case class RegisteredComponents(componentIds: Set[String])

  /**
    * Used by LogReceiverActor towards LogReceiver to request processing of logs of a component
    */
  case class SubmitLogs(componentId: String, logs: String, metricKey: String)

  /**
    * Used by AdministratorActor towards LogParserActor to request its details
    */
  case object RequestDetails

  /**
    * Used by LogParserActor towards AdministratorActor to return its details
    */
  case class Details(metrics: List[Metric])

  /**
    * Used by Administrator towards LogReceiver to notify it of a new LogReceiver actor
    */
  case class RegisterNewLogParser(componentId: String, actor: ActorRef)

  /**
    * Used to register a metric in the LogParserActor
    */
  case class Metric(metricKey: String, regex: String)

  /**
    * Used to register a metric in the AdministratorParserActor
    */
  case class RegisterMetric(componentId: String, metric: Metric)

  /**
    * Used by LogParserActor to signal a metric was registered
    */
  case class MetricRegistered(metric: Metric)

  object Rules {

    /**
      * Encapsulates a basic alerting rule.
      */
    case class AlertingRule(threshold: Threshold, action: Action)

    /** Defines threshold of a rule. */
    case class Threshold(max: String) {
      def limit: Duration = Duration(max)
    }
    case class Action(url: String)
  }

  /**
    * Used by the Administrator towards AdministratorActor to add a new alerting rule.
    */
  case class RegisterNewAlertingRule(componentId: String, metricKey: String, rule: AlertingRule)

  /**
    * Used by AdministratorActor towards Administrator to indicate that the given rule was successfully created.
    */
  case class AlertingRuleCreated(rule: AlertingRule)

  /** Used for submitting a new log to measure via LogReceiver. */
  case class LogMeasurable(logs: String, metricKey: String)

  case class CheckRuleBreak(logs: String, metric: Metric)
}

trait Protocol extends DefaultJsonProtocol {
  implicit val metricFormatter = jsonFormat(Metric.apply, "metric-key", "regex")
  implicit val detailsFormatter = jsonFormat1(Details.apply)
  implicit val registerComponentsFormatter = jsonFormat1(RegisterComponent.apply)
  implicit val registeredComponentsFormatter = jsonFormat1(RegisteredComponents.apply)

  implicit val thresholdRuleFormatter = jsonFormat1(Rules.Threshold.apply)
  implicit val actionRuleFormatter = jsonFormat1(Rules.Action.apply)
  implicit val alertingRuleFormatter = jsonFormat2(Rules.AlertingRule.apply)

  implicit val logMeasurableFormatter = jsonFormat(LogMeasurable.apply, "metric-key", "log")
}
