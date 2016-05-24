package performanceanalysis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.{AlertingRuleCreated, CheckRuleBreak, _}

import scala.util.matching.Regex

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  type MetricKey = String

  def props(dateTimeParser: DateTimeParser = DateTimeParser.mdy): Props = Props(new LogParserActor(dateTimeParser) with AlertRuleActorCreator)
}

class LogParserActor(dateTimeParser: DateTimeParser = DateTimeParser.mdy) extends Actor with ActorLogging {
  this: AlertRuleActorCreator =>
  private type AlertRuleActorRef = ActorRef

  def receive: Receive = normal(Nil, Map())

  def normal(metrics: List[Metric], alertsByMetricKey: Map[MetricKey, List[AlertRuleActorRef]]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender() ! Details(metrics)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      context.become(normal(metric :: metrics, alertsByMetricKey))
      sender() ! MetricRegistered(metric)

    case msg: SubmitLog =>
      handleSubmitLog(msg, metrics, alertsByMetricKey)

    case RegisterAlertingRule(compId, metricKey, rule) =>
      log.debug("received new alert rule {} in {}", rule, self.path)

      findMetric(metricKey, metrics) match {
        case None => sender() ! MetricNotFound(compId, metricKey)
        case Some(metric) =>
          sender() ! AlertingRuleCreated(compId, metricKey, rule)
          val newAlertActorRef = create(context, rule, compId, metricKey)
          context.become(normal(metrics, updateAlertsByMetricKey(alertsByMetricKey, newAlertActorRef, metricKey)))
      }

  }

  private def findMetric(metricKey: MetricKey, metrics: List[Metric]):Option[Metric] = {
    metrics.find(_.metricKey == metricKey)
  }

  private def updateAlertsByMetricKey(alertsByMetricKey: Map[MetricKey, List[AlertRuleActorRef]],
                                      newAlertRuleActorRef: AlertRuleActorRef, key: MetricKey) = {
    alertsByMetricKey + (key -> (newAlertRuleActorRef :: alertsByMetricKey.getOrElse(key, Nil)))
  }

  private def handleSubmitLog(msg: SubmitLog, metrics: List[Metric],
                              alertsByMetricKey: Map[MetricKey, List[AlertRuleActorRef]]) {
    log.debug("received {} in {}", msg, self.path)
    metrics.foreach { metric =>
      val parseResult = parseLogLine(msg.logLine, metric)
      for {
        value <- parseResult.metric
        alertRuleActorRef <- alertsByMetricKey(metric.metricKey)
      } {
        log.info("sending {} to {}", CheckRuleBreak(value), alertRuleActorRef.path)
        alertRuleActorRef ! CheckRuleBreak(value)
      }
    }
  }

  private def parseLogLine(logLine: String, metric: Metric): ParsedLine = {
    val pattern: Regex = metric.regex.r
    val parser = LineParser(pattern)
    parser.parse(logLine)
  }

}
