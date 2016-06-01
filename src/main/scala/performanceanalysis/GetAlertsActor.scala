package performanceanalysis

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import performanceanalysis.server.Protocol.{RequestAlertRuleDetails, AllAlertRuleDetails, SingleAlertRuleDetails}
import performanceanalysis.server.Protocol.Rules.AlertRule
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

  for (alertActor <- alertActors) {
    alertActor ! RequestAlertRuleDetails
  }

  context.system.scheduler.scheduleOnce(GetAlertsActor.timeOut, self, "timeout")

  private def sendAndStop(ruleSet: Set[AlertRule]) = {
    originalSender ! AllAlertRuleDetails(ruleSet)
    context.stop(self)
  }

  def normal(ruleSet: Set[AlertRule], countdown:Int): Receive = {
    case "timeout" =>
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
