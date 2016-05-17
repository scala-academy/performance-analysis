package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol.Rules.AlertingRule
import performanceanalysis.server.Protocol.{Action, CheckRuleBreak}

import scala.concurrent.duration._

object AlertRuleActor {

  def props(alertingRule: AlertingRule, componentId: String, metricKey: String): Props =
    Props.apply(new AlertRuleActor(alertingRule, componentId, metricKey) with AlertActionActorCreator)
}

class AlertRuleActor(alertingRule: AlertingRule, componentId: String, metricKey: String) extends Actor with ActorLogging {

  this: AlertActionActorCreator =>

  lazy val actionActor: ActorRef = create(context)

  override def receive: Receive = {
    case CheckRuleBreak(value) if value.isInstanceOf[Duration] && doesBreakRule(value.asInstanceOf[Duration]) =>
      log.info(s"Rule $alertingRule is broken for $componentId/$metricKey")
      actionActor ! Action(alertingRule.action.url,
      s"Rule $alertingRule was broken for component id $componentId and metric key $metricKey")
  }

  private def doesBreakRule(duration: Duration) = {
    log.info(s"Checking if $duration breaks $alertingRule")
    duration > alertingRule.threshold.limit
  }

}
