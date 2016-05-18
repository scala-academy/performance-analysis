package performanceanalysis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.InterActorMessage.{CheckRuleBreak, Details}
import performanceanalysis.server.Protocol.{AlertingRuleCreated, _}

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

  def normal(metrics: List[Metric], metricToAlertRuleActors: Map[MetricKey, List[ActorRef]]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      context.become(normal(metric :: metrics, metricToAlertRuleActors))
      sender ! MetricRegistered(metric)

    case msg: SubmitLogs =>
      handleSubmitLogs(msg, metrics, metricToAlertRuleActors)

    case msg: RegisterAlertingRule =>
      log.debug("received new alert rule {} in {}", msg, self.path)

      metricWithKey(msg.metricKey, metrics) match {
        case None => sender() ! MetricNotFound(msg.componentId, msg.metricKey)
        case Some(metric) =>
          sender() ! AlertingRuleCreated(msg.componentId, msg.metricKey, msg.rule)
          context.become(normal(metrics, updateMetricToAlertRuleActors(metricToAlertRuleActors, msg)))
      }

  }

  private def metricWithKey(metricKey: String, metrics: List[Metric]) =
    metrics.find(_.metricKey == metricKey)

  private def updateMetricToAlertRuleActors(metricToAlertActions: Map[MetricKey, List[ActorRef]], msg: RegisterAlertingRule) = {
    val alertRuleActor = create(context, msg.rule, msg.componentId, msg.metricKey)
    val updatedActionActorsForMetric: List[ActorRef] = alertRuleActor :: metricToAlertActions.getOrElse(msg.metricKey, Nil)
    metricToAlertActions + (msg.metricKey -> updatedActionActorsForMetric)
  }

  private def handleSubmitLogs(msg: SubmitLogs, metrics: List[Metric], metricToAlertActions: Map[MetricKey, List[ActorRef]]) {
    log.debug("received {} in {}", msg, self.path)
    for (metric <- metrics) {
      val parsedValue = parseLogLine(msg.logs, metric)

      for (value <- parsedValue;
           alertActionActor <- metricToAlertActions(metric.metricKey)) {
        log.info("sending {} to {}", CheckRuleBreak(value), alertActionActor.path)
        alertActionActor ! CheckRuleBreak(value)
      }

    }
  }

  private def parseLogLine(log: String, metric: Metric) = metric.regex.r.findFirstIn(log)

}
