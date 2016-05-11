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

  def receive: Receive = normal(Nil, Map())

  def normal(metrics: List[Metric], alertsByMetricKey: Map[MetricKey, List[Monitor]]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      context.become(normal(metric :: metrics, alertsByMetricKey))
      sender ! MetricRegistered(metric)

    case msg: SubmitLog =>
      handleSubmitLog(msg, metrics, alertsByMetricKey)

    case RegisterAlertingRule(compId, metricKey, rule) =>
      log.debug("received new alert rule {} in {}", rule, self.path)

      findMetric(metricKey, metrics) match {
        case None => sender() ! MetricNotFound(compId, metricKey)
        case Some(metric) =>
          sender() ! AlertingRuleCreated(compId, metricKey, rule)
          val newMonitor = create(context, rule, compId, metricKey)
          context.become(normal(metrics, updateMonitors(alertsByMetricKey, newMonitor, metricKey)))
      }

  }

  private def findMetric(metricKey: MetricKey, metrics: Metrics): Option[Metric] = {
    metrics.find(_.metricKey == metricKey)
  }

  private def updateMonitors(monitors: Map[MetricKey, List[Monitor]], newMonitor: Monitor, key: MetricKey) = {
    monitors + (key -> (newMonitor :: monitors.getOrElse(key, Nil)))
  }

  private def handleSubmitLog(msg: SubmitLog, metrics: Metrics, monitors: Map[MetricKey, List[Monitor]]) {
    log.debug("received {} in {}", msg, self.path)
    metrics.foreach { metric =>
      val parseResult = parseLogLine(msg.logLine, metric)
      for {
        value <- parseResult.metric
        monitor <- monitors(metric.metricKey)
      } {
        log.info("sending {} to {}", CheckRuleBreak(value), monitor.path)
        monitor ! CheckRuleBreak(value)
      }
    }
  }

  private def parseLogLine(logLine: String, metric: Metric): ParsedLine = {
    val pattern: Regex = metric.regex.r
    val parser = LineParser(pattern)
    parser.parse(logLine)
  }

}
