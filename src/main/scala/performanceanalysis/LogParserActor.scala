package performanceanalysis

import akka.actor._
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.{AlertingRuleCreated, CheckRuleBreak, _}


/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  type MetricKey = String
  def props: Props = Props(new LogParserActor() with AlertRuleActorCreator)
}

class LogParserActor extends Actor with ActorLogging {

  this: AlertRuleActorCreator =>

  def receive: Receive = normal(Nil, Map())

  def normal(metrics: List[Metric], alertsByMetric: Map[MetricKey, List[ActorRef]]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case RequestAlertRules(metricKey) =>
      log.debug("received request for alert rules of {}", metricKey)
      handleGetAlertRules(alertsByMetric, metricKey)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      context.become(normal(metric :: metrics, alertsByMetric))
      sender() ! MetricRegistered(metric)

    case msg: SubmitLogs =>
      handleSubmitLogs(msg, metrics, alertsByMetric)

    case msg: DeleteAllAlertingRules =>
      handleDeleteRules(metrics, alertsByMetric, msg)

    case msg: RegisterAlertingRule =>
      log.debug("received new alert rule {} in {}", msg, self.path)

      metricWithKey(msg.metricKey, metrics) match {
        case None => sender() ! MetricNotFound(msg.componentId, msg.metricKey)
        case Some(metric) =>
          context.become(normal(metrics, updateAlertsByMetric(alertsByMetric, msg)))
          sender() ! AlertingRuleCreated(msg.componentId, msg.metricKey, msg.rule)
      }
  }

  private def metricWithKey(metricKey: String, metrics: List[Metric]) =
    metrics.find(_.metricKey == metricKey)

  private def updateAlertsByMetric(alertsByMetric: Map[MetricKey, List[ActorRef]], msg: RegisterAlertingRule) = {
    val alertRuleActor = create(context, msg.rule, msg.componentId, msg.metricKey)
    val updatedActionActorsForMetric: List[ActorRef] = alertRuleActor :: alertsByMetric.getOrElse(msg.metricKey, Nil)
    alertsByMetric + (msg.metricKey -> updatedActionActorsForMetric)
  }

  private def handleGetAlertRules(alertsByMetric: Map[MetricKey, List[ActorRef]], metricKey: String) = {

    alertsByMetric.get(metricKey) match {
      case None =>
        sender() ! MetricNotFound
      case Some(ruleList) =>
        context.actorOf(GetAlertsActor.props(ruleList, sender()))
    }
  }

  private def handleDeleteRules(metrics: List[Metric], alertsByMetric: Map[MetricKey, List[ActorRef]], msg: DeleteAllAlertingRules) = {
    log.debug("received delete request for all rules on metric {}", msg.metricKey)

    alertsByMetric.get(msg.metricKey) match {
      case None =>
        sender() ! MetricNotFound(msg.componentId, msg.metricKey)
      case Some(ruleList) =>
        for (ruleActor <- ruleList) {
          context.stop(ruleActor)
        }

        context.become(normal(metrics, alertsByMetric - msg.metricKey))
        sender() ! AlertRulesDeleted(msg.componentId)
    }
  }

  private def handleSubmitLogs(msg: SubmitLogs, metrics: List[Metric], alertsByMetric: Map[MetricKey, List[ActorRef]]) {
    log.debug("received {} in {}", msg, self.path)
    for (metric <- metrics) {
      val parsedValue = parseLogLine(msg.logs, metric)

      for (value <- parsedValue;
           alertActionActor <- alertsByMetric(metric.metricKey)) {
        log.info("sending {} to {}", CheckRuleBreak(value), alertActionActor.path)
        alertActionActor ! CheckRuleBreak(value)
      }
    }
  }

  private def parseLogLine(log: String, metric: Metric) = metric.regex.r.findFirstIn(log)

}
