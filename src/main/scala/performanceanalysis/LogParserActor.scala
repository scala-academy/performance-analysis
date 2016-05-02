package performanceanalysis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.LogParserActor.MetricToAlertActions
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.{AlertingRuleCreated, CheckRuleBreak, _}

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  type MetricToAlertActions = Map[String, List[ActorRef]]

  def props: Props = Props(new LogParserActor() with AlertRuleActorCreator)
}

class LogParserActor extends Actor with ActorLogging {

  this: AlertRuleActorCreator =>

  def receive: Receive = normal(Nil, Map())

  def normal(metrics: List[Metric], metricToAlertActions: MetricToAlertActions): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case metric: Metric =>
      log.debug(s"received post with metric $metric")
      context.become(normal(metric :: metrics, metricToAlertActions))
      sender ! MetricRegistered(metric)

    case msg: SubmitLogs =>
      handleSubmitLogs(msg, metrics, metricToAlertActions)

    case msg: RegisterAlertingRule =>
      log.debug(s"received new alert rule $msg in ${self.path}")

      metricWithKey(msg.metricKey, metrics) match {
        case None => sender() ! MetricNotFound(msg.componentId, msg.metricKey)
        case Some(metric) =>
          sender() ! AlertingRuleCreated(msg.componentId, msg.metricKey, msg.rule)
          context.become(normal(metrics, updateMetricToAlterActions(metricToAlertActions, msg)))
      }

  }

  private def metricWithKey(metricKey: String, metrics: List[Metric]) =
    metrics.find(_.metricKey == metricKey)

  private def updateMetricToAlterActions(metricToAlertActions: MetricToAlertActions, msg: RegisterAlertingRule) = {
    val alertRuleActor = create(context, msg.rule, msg.componentId, msg.metricKey)
    val updatedActionActorsForMetric: List[ActorRef] = alertRuleActor :: metricToAlertActions.getOrElse(msg.metricKey, List())
    metricToAlertActions + (msg.metricKey -> updatedActionActorsForMetric)
  }

  private def handleSubmitLogs(msg: SubmitLogs, metrics: List[Metric], metricToAlertActions: MetricToAlertActions) {
    log.debug(s"received $msg in ${self.path}")
    for (metric <- metrics) {
      val parsedValue = parseLogLine(msg.logs, metric)

      for (value <- parsedValue;
           alertActionActor <- metricToAlertActions(metric.metricKey)) {
        log.info(s"sending ${CheckRuleBreak(value)} to ${alertActionActor.path}")
        alertActionActor ! CheckRuleBreak(value)
      }

    }
  }

  private def parseLogLine(log: String, metric: Metric) = metric.regex.r.findFirstIn(log)

}
