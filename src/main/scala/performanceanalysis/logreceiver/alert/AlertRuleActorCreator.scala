package performanceanalysis.logreceiver.alert

import akka.actor.{ActorContext, ActorRef}
import performanceanalysis.server.Protocol.Metric
import performanceanalysis.server.Protocol.Rules.AlertingRule

trait AlertRuleActorCreator {

  def create(context: ActorContext, rule: AlertingRule, componentId: String, metric: Metric): ActorRef =
    context.actorOf(AlertRuleActor.props(rule, componentId, metric))
}
