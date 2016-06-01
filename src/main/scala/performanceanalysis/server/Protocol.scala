package performanceanalysis.server

import akka.actor.ActorRef
import performanceanalysis.server.Protocol._
import spray.json._

import scala.concurrent.duration.Duration

object Protocol {

  /**
    * Used by Administrator towards AdministratorActor to register a new component
    */
  case class RegisterComponent(componentId: String, dateFormat: Option[String] = None)

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
    * Used by the Administrator to request all posted logLines for a component
    */
  case class GetComponentLogLines(componentId: String)

  /**
    * Used by the Administrator to request parsed logLines for a component and metricKey
    */
  case class GetParsedLogLines(componentId: String, metricKey: String)
  /**
    * Used by Administrator towards AdministratorActor to request AlertRules of a metric of a component
    */
  case class GetAlertRules(componentId: String, metricKey: String)

  /**
    * Used by AdministratorActor to request AlertRules of a component
    */
  case class RequestAlertRules(metricKey: String)

  /**
    * Used by GetAlertsActor to request details from AlertRuleActor
    */
  case object RequestAlertRuleDetails

  /**
    * Used by AlertRuleActor to send its details to GetAlertsActor
    */
  case class SingleAlertRuleDetails(alertRule: Rules.AlertRule)

  /**
    * Used by GetAlertsActor to send the collected AlertRules to AdministratorActor
    */
  case class AllAlertRuleDetails(alertRules: Set[Rules.AlertRule])

  /**
    * Used by LogParserActor to indicate to AdministratorActor that no Alerts are registered for this metric
    */
  case class NoAlertsFound(componentId:String, metricKey: String)

  /**
    * Used by Administrator towards AdministratorActor to request a list of all registered components
    */
  case object GetRegisteredComponents

  /**
    * Used by AdministratorActor towards Administrator to return a list of all registered components
    */
  case class RegisteredComponents(componentIds: Set[String])

  /**
    * Used by LogReceiverActor towards LogReceiver to request processing of single log of a component
    */
  case class SubmitLog(componentId: String, logLine: String)

  /**
    * Used by LogReceiverActor to signal log submitted
    */
  case object LogSubmitted

  /**
    * Used by AdministratorActor towards LogParserActor to request its details
    */
  case object RequestDetails

  /**
    * Used by AdministratorActor towards LogParserActor to request its log lines
    */
  case object RequestComponentLogLines

  /**
    * Used by AdministratorActor towards LogParserActor to request its parsed log lines
    */
  case class RequestParsedLogLines(metricKey: String)
  /**
    * Used by LogParserActor towards AdministratorActor to return its details
    */
  case class Details(metrics: List[Metric])
  /**
    * Used by LogParserActor towards AdministratorActor to return its log lines
    */
  case class ComponentLogLines(logLines: List[String])
  /**
    * Used by Administrator towards LogReceiver to notify it of a new LogReceiver actor
    */
  case class RegisterNewLogParser(componentId: String, actor: ActorRef, dateFormat: Option[String])

  /**
    * The value type of a Metric
    */
  case class ValueType(aType: Any)

  /**
    * Used to register a metric in the LogParserActor
    */
  case class Metric(metricKey: String, regex: String, valueType: ValueType = ValueType(classOf[String]))

  /**
    * Used to register a metric in the AdministratorParserActor
    */
  case class RegisterMetric(componentId: String, metric: Metric)

  /**
    * Used by LogParserActor to signal a metric was registered
    */
  case class MetricRegistered(metric: Metric)

  /**
    * Used by LogParserActor to indicated that requested metrics is not registered.
    */
  case class MetricNotFound(componentId: String, metricKey: String)

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

  /**
    * Used by the Administrator towards AdministratorActor to add a new alerting rule.
    */
  case class RegisterAlertRule(componentId: String, metricKey: String, rule: Rules.AlertRule)

  /**
    * Used by AdministratorActor towards Administrator to indicate that the given rule was successfully created.
    */
  case class AlertRuleCreated(componentId: String, metricKey: String, rule: Rules.AlertRule)

  /**
    * Used by the Administrator towards AdministratorActor to delete all alerting rules
    */
  case class DeleteAllAlertingRules(componentId: String, metricKey: String)

  /**
    * Used by AdministratorActor towards Administrator to indicate that the rules were successfully deleted.
    */
  case class AlertRulesDeleted(componentId: String)

  /**
    * Used by LogParserActor to trigger an alert action check. Message handled by AlerRuleActor.
    */
  case class CheckRuleBreak(value: Any)

  /**
    * Used by ActionAlertActor to trigger an action when a rule breaks. Handled by AlertActionActor.
    */
  case class Action(url: String, message: String)
}

trait Protocol extends DefaultJsonProtocol {

  implicit object valueTypeFormat extends JsonFormat[ValueType] {
    def read(value: JsValue): ValueType = value match {
      case JsString("string") => ValueType(classOf[String])
      case JsString("boolean") => ValueType(classOf[Boolean])
      case JsString("duration") => ValueType(classOf[Duration])
      case _ => deserializationError("Unknown value type")
    }
    def write(f: ValueType): JsValue = f.aType match {
      case c if c == classOf[String] => JsString("string")
      case c if c == classOf[Boolean] => JsString("boolean")
      case c if c == classOf[Duration] => JsString("duration")
      case _ => serializationError("Unknown value type")
    }
  }

  implicit val metricFormatter = jsonFormat(Metric.apply, "metric-key", "regex", "value-type")
  implicit val detailsFormatter = jsonFormat1(Details.apply)
  implicit val registerComponentsFormatter = jsonFormat2(RegisterComponent.apply)
  implicit val registeredComponentsFormatter = jsonFormat1(RegisteredComponents.apply)

  implicit val thresholdRuleFormatter = jsonFormat1(Rules.Threshold.apply)
  implicit val actionRuleFormatter = jsonFormat1(Rules.Action.apply)
  implicit val alertingRuleFormatter = jsonFormat2(Rules.AlertRule.apply)

  implicit val alertRulesDetailsFormatter = jsonFormat1(AllAlertRuleDetails.apply)
}
