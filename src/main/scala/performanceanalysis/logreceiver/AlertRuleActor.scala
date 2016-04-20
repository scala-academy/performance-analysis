package performanceanalysis.logreceiver

import akka.actor.{Actor, ActorLogging, Props}
import performanceanalysis.server.Protocol.{CheckRuleBreak, Metric}
import performanceanalysis.server.Protocol.Rules.AlertingRule

object AlertRuleActor {

  def props(alertingRule: AlertingRule): Props = Props.apply(new AlertRuleActor(alertingRule))
}

class AlertRuleActor(alertingRule: AlertingRule) extends Actor with ActorLogging {

  override def receive: Receive = {
    case msg: CheckRuleBreak if doesBreakRule(msg.logs, msg.metric) =>
  }

  def doesBreakRule(log: String, metric: Metric) = true; //TODO - change for real implementation
}
