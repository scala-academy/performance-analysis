package performanceanalysis.server

import performanceanalysis.server.Protocol._
import performanceanalysis.server.messages.AdministratorMessages.{RegisterComponent, RegisteredComponents}
import performanceanalysis.server.messages.AlertMessages.AllAlertRuleDetails
import performanceanalysis.server.messages.LogMessages.{Details, Metric}
import performanceanalysis.server.messages.Rules
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, _}

import scala.concurrent.duration.Duration

object Protocol {

  /**
    * The value type of a Metric
    */
  case class ValueType(aType: Any)

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
