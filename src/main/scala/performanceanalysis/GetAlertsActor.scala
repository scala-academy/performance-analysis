package performanceanalysis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.messages.AlertMessages._
import performanceanalysis.server.messages.Rules.AlertRule
import scala.concurrent.duration._

/**
  * Created by steven on 17-5-16.
  */
object GetAlertsActor {
  def props(ruleList: List[ActorRef], originalSender: ActorRef): Props = Props(new GetAlertsActor(ruleList, originalSender))

  val timeOut = 4.seconds
}

class GetAlertsActor(alertActors: List[ActorRef], originalSender: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  // Private so that only this actor can send this message
  private case object Timeout

  for (alertActor <- alertActors) {
    alertActor ! RequestAlertRuleDetails
  }

  context.system.scheduler.scheduleOnce(GetAlertsActor.timeOut, self, Timeout)

  private def sendAndStop(ruleSet: Set[AlertRule]) = {
    originalSender ! AllAlertRuleDetails(ruleSet)
    context.stop(self)
  }

  def normal(ruleSet: Set[AlertRule], countdown:Int): Receive = {
    case Timeout =>
      log.debug("Timeout! Sending results so far to {}", originalSender)
      sendAndStop(ruleSet)
    case SingleAlertRuleDetails(alertRule) =>
      val newCountdown = countdown - 1
      val newRuleSet = ruleSet + alertRule

      if (newCountdown == 0) {
        log.debug("DONE, sending answer to {}", originalSender)
        sendAndStop(newRuleSet)
      }

      context.become(normal(newRuleSet, newCountdown))
  }

  def receive:Receive = normal(Set.empty[AlertRule], alertActors.length)
}
