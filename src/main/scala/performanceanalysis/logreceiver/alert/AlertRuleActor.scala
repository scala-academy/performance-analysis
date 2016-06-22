package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.messages.AlertMessages._
import performanceanalysis.server.messages.LogMessages.Metric
import performanceanalysis.server.messages.Rules.AlertRule

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object AlertRuleActor {

  def props(alertingRule: AlertRule, componentId: String, metric: Metric): Props =
    Props.apply(new AlertRuleActor(alertingRule, componentId, metric) with AlertActionActorCreator)
}

class AlertRuleActor(alertingRule: AlertRule, componentId: String, metric: Metric) extends Actor with ActorLogging{

  this: AlertActionActorCreator =>

  lazy val actionActor: ActorRef = create(context)

  override def receive: Receive = {
    case CheckRuleBreak(value: Duration) if doesBreakRule(value) =>
      log.info("Rule {} is broken for {}/{}", alertingRule, componentId, metric)
      actionActor ! AlertRuleViolated(alertingRule.action.url, alertMessage(value))
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
