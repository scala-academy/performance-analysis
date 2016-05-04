package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, Props}

object AlertActionActor {

  def props(): Props = Props.apply(new AlertActionActor)
}

class AlertActionActor extends Actor with ActorLogging {

  override def receive: Receive = ???

}
