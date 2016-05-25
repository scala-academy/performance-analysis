package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol.Rules.AlertRule
import performanceanalysis.server.Protocol._

import scala.util.{Failure, Success}

object AlertRuleActor {

  def props(alertingRule: AlertRule, componentId: String, metric: Metric): Props =
    Props.apply(new AlertRuleActor(alertingRule, componentId, metric) with AlertActionActorCreator)
}

class AlertRuleActor(alertingRule: AlertRule, componentId: String, metric: Metric) extends Actor with ActorLogging{

  this: AlertActionActorCreator =>

  lazy val actionActor: ActorRef = create(context)

  override def receive: Receive = {
    case CheckRuleBreak(value) if doesBreakRule(value) =>
      log.info("Rule {} is broken for {}/{}", alertingRule, componentId, metric)
      actionActor ! Action(alertingRule.action.url, alertMessage(value))
    case RequestAlertRuleDetails =>
      sender() ! SingleAlertRuleDetails(alertingRule)
  }

  private def doesBreakRule(value: Any) = {
    log.debug("Checking if {} breaks {}", value, alertingRule)
    alertingRule.expression match {
      case Success(e) => e.evaluate(value, metric.valueType)
      case Failure(ex) => false
    }
  }

  private def alertMessage(value: Any) =
    s"Rule $alertingRule was broken for component id $componentId and metric $metric with value $value"
}
