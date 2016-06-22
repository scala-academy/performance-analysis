package performanceanalysis.logreceiver.alert

import akka.actor.{ActorContext, ActorRef}
import performanceanalysis.server.messages.LogMessages.Metric
import performanceanalysis.server.messages.Rules.AlertRule

trait AlertRuleActorCreator {

  def create(context: ActorContext, rule: AlertRule, componentId: String, metric: Metric): ActorRef =
    context.actorOf(AlertRuleActor.props(rule, componentId, metric))
}
