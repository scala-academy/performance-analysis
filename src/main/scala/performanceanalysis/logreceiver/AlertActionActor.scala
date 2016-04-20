package performanceanalysis.logreceiver

import akka.actor.{Actor, ActorLogging, Props}
import performanceanalysis.server.Protocol.{CheckRuleBreak, Metric}
import performanceanalysis.server.Protocol.Rules.AlertingRule

object AlertActionActor {

  def props(alertingRule: AlertingRule): Props = Props.apply(new AlertActionActor)
}

class AlertActionActor extends Actor with ActorLogging {

  override def receive: Receive = ???

}
