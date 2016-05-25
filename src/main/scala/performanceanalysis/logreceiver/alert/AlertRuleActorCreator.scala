package performanceanalysis.logreceiver.alert

import akka.actor.{ActorContext, ActorRef}
import performanceanalysis.server.Protocol.Rules.AlertRule

trait AlertRuleActorCreator {

  def create(context: ActorContext, rule: AlertRule, componentId: String, metricKey: String): ActorRef =
    context.actorOf(AlertRuleActor.props(rule, componentId, metricKey))
}
