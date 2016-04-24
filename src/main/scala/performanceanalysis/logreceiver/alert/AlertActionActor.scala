package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, Props}
import performanceanalysis.server.Protocol.Rules

object AlertActionActor {

  def props(alertingRule: Rules.AlertingRule): Props = Props.apply(new AlertActionActor)
}

class AlertActionActor extends Actor with ActorLogging {

  override def receive: Receive = ???

}
