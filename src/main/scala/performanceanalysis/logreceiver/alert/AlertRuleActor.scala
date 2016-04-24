package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol.Rules.AlertingRule
import performanceanalysis.server.Protocol.{Action, CheckRuleBreak, Metric}

object AlertRuleActor {

  def props(alertingRule: AlertingRule): Props = Props.apply(new AlertRuleActor(alertingRule))
}

class AlertRuleActor(alertingRule: AlertingRule) extends Actor with ActorLogging {

  val actionActor: ActorRef = ???;

  override def receive: Receive = {
    case msg: CheckRuleBreak if doesBreakRule(msg.logs, msg.metric) => actionActor ! Action("an url here", "a message here")
  }

  def doesBreakRule(log: String, metric: Metric) = true; //TODO - change for real implementation
}
