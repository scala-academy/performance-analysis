package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol.Rules.AlertRule
import performanceanalysis.server.Protocol.{AlertRuleDetails, GetDetails, Action, CheckRuleBreak}

import scala.concurrent.duration._

object AlertRuleActor {

  def props(alertingRule: AlertRule, componentId: String, metricKey: String): Props =
    Props.apply(new AlertRuleActor(alertingRule, componentId, metricKey) with AlertActionActorCreator)
}

class AlertRuleActor(alertingRule: AlertRule, componentId: String, metricKey: String) extends Actor with ActorLogging {

  this: AlertActionActorCreator =>

  lazy val actionActor: ActorRef = create(context)

  override def receive: Receive = {
    case msg: CheckRuleBreak if doesBreakRule(msg.value) =>
      log.info(s"Rule $alertingRule is broken for $componentId/$metricKey")
      actionActor ! Action(alertingRule.action.url,
      s"Rule $alertingRule was broken for component id $componentId and metric key $metricKey")
    case GetDetails(_) =>
      sender() ! AlertRuleDetails(alertingRule)
  }

  private def doesBreakRule(value: String) = {
    log.info(s"Checking if $value breaks $alertingRule")
    Duration(value) > alertingRule.threshold.limit
  }

}
