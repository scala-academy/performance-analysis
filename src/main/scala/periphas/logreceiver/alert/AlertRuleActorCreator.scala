package periphas.logreceiver.alert

import akka.actor.{ActorContext, ActorRef}
import periphas.server.Protocol.Rules.AlertingRule

trait AlertRuleActorCreator {

  def create(context: ActorContext, rule: AlertingRule, componentId: String, metricKey: String): ActorRef =
    context.actorOf(AlertRuleActor.props(rule, componentId, metricKey))
}
