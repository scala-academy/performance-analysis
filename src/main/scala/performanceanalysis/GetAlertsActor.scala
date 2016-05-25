package performanceanalysis

import akka.actor.{ActorLogging, Actor, Props, ActorRef}
import performanceanalysis.server.Protocol.{RequestAlertRuleDetails, AllAlertRuleDetails, SingleAlertRuleDetails}
import performanceanalysis.server.Protocol.Rules.AlertRule

/**
  * Created by steven on 17-5-16.
  */
object GetAlertsActor {
  def props(ruleList: List[ActorRef], originalSender: ActorRef): Props = Props(new GetAlertsActor(ruleList, originalSender))
}

class GetAlertsActor(alertActors: List[ActorRef], originalSender: ActorRef) extends Actor with ActorLogging {

  for (alertActor <- alertActors) {
    alertActor ! RequestAlertRuleDetails
  }

  def normal(ruleSet: Set[AlertRule], countdown:Int): Receive = {
    case SingleAlertRuleDetails(alertRule) =>
      val newCountdown = countdown - 1
      val newRuleSet = ruleSet + alertRule

      if (newCountdown == 0) {
        log.debug("DONE, sending answer to {}", originalSender)
        originalSender ! AllAlertRuleDetails(newRuleSet)
        context.stop(self)
      }
      context.become(normal(newRuleSet, newCountdown))
  }

  def receive:Receive = normal(Set.empty[AlertRule], alertActors.length)
}
