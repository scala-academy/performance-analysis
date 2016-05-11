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

  def props: Props = Props(new LogParserActor() with AlertRuleActorCreator)
}

class LogParserActor extends Actor with ActorLogging {
  this: AlertRuleActorCreator =>
  private type Metrics = List[Metric]
  private type Monitor = ActorRef
  private type Monitors = Map[MetricKey, List[Monitor]]

  def receive: Receive = normal(Nil, Map())

  def normal(metrics: Metrics, monitors: Monitors): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      context.become(normal(metric :: metrics, monitors))
      sender ! MetricRegistered(metric)

    case msg: SubmitLog =>
      handleSubmitLog(msg, metrics, monitors)

    case RegisterAlertingRule(compId, metricKey, rule) =>
      log.debug("received new alert rule {} in {}", rule, self.path)

      findMetric(metricKey, metrics) match {
        case None => sender() ! MetricNotFound(compId, metricKey)
        case Some(metric) =>
          sender() ! AlertingRuleCreated(compId, metricKey, rule)
          val newMonitor = create(context, rule, compId, metricKey)
          context.become(normal(metrics, updateMonitors(monitors, newMonitor, metricKey)))
      }

  }

  private def findMetric(metricKey: MetricKey, metrics: Metrics): Option[Metric] = {
    metrics.find(_.metricKey == metricKey)
  }

  private def updateMonitors(monitors: Monitors, newMonitor: Monitor, key: MetricKey): Monitors = {
    monitors + (key -> (newMonitor :: monitors.getOrElse(key, Nil)))
  }

  private def handleSubmitLog(msg: SubmitLog, metrics: Metrics, monitors: Monitors) {
    log.debug("received {} in {}", msg, self.path)
    for {
      metric <- metrics
      value <- parseLogLine(msg.logLine, metric).metric
      monitor <- monitors(metric.metricKey)
    } {
      log.info("sending {} to {}", CheckRuleBreak(value), monitor.path)
      monitor ! CheckRuleBreak(value)
    }
  }

  private def parseLogLine(logLine: String, metric: Metric): ParsedLine = {
    val pattern: Regex = metric.regex.r
    val parser = LineParser(pattern)
    parser.parse(logLine)
  }

}
