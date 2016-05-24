package performanceanalysis.logreceiver.alert

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.rules.ExpressionEvaluator
import performanceanalysis.server.Protocol.Rules.AlertingRule
import performanceanalysis.server.Protocol.{Action, CheckRuleBreak, Metric}

import scala.util.{Failure, Success}

object AlertRuleActor {

  def props(alertingRule: AlertingRule, componentId: String, metric: Metric): Props =
    Props.apply(new AlertRuleActor(alertingRule, componentId, metric) with AlertActionActorCreator)
}

class AlertRuleActor(alertingRule: AlertingRule, componentId: String, metric: Metric) extends Actor
  with ActorLogging with ExpressionEvaluator {

  this: AlertActionActorCreator =>

  lazy val actionActor: ActorRef = create(context)

  override def receive: Receive = {
    case CheckRuleBreak(value) if doesBreakRule(value) =>
      log.info("Rule {} is broken for {}/{}", alertingRule, componentId, metric)
      actionActor ! Action(alertingRule.action.url, alertMessage(value))
  }

  private def doesBreakRule(value: Any) = {
    log.debug("Checking if {} breaks {}", value, alertingRule)
    alertingRule.expression match {
      case Success(e) => evaluate(e, value, metric.valueType)
      case Failure(ex) => false
    }
  }

  private def alertMessage(value: Any) =
    s"Rule $alertingRule was broken for component id $componentId and metric $metric with value $value"
}
