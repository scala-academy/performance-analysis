package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.messages.AlertMessages._
import performanceanalysis.server.messages.Rules.AlertRule

import scala.concurrent.duration._

object AlertRuleActor {

  def props(alertingRule: AlertRule, componentId: String, metricKey: String): Props =
    Props.apply(new AlertRuleActor(alertingRule, componentId, metricKey) with AlertActionActorCreator)
}

class AlertRuleActor(alertingRule: AlertRule, componentId: String, metricKey: String) extends Actor with ActorLogging {

  this: AlertActionActorCreator =>

  lazy val actionActor: ActorRef = create(context)

  override def receive: Receive = {
    case CheckRuleBreak(value:Duration) if doesBreakRule(value) =>
      log.info("Rule {} is broken for {}/{}", alertingRule, componentId, metricKey)
      actionActor ! AlertRuleViolated(alertingRule.action.url, alertMessage(value))
    case RequestAlertRuleDetails =>
      sender() ! SingleAlertRuleDetails(alertingRule)
  }

  private def doesBreakRule(duration: Duration) = {
    log.debug("Checking if {} breaks {}", duration, alertingRule)
    duration > alertingRule.threshold.limit
  }

  private def alertMessage(value: Duration) =
    s"Rule $alertingRule was broken for component id $componentId and metric key $metricKey with value $value"
}
