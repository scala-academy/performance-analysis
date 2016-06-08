package performanceanalysis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.messages.Rules.AlertRule
import performanceanalysis.server.messages.AlertMessages._
import performanceanalysis.server.messages.LogMessages._
import performanceanalysis.server._

import scala.util.matching.Regex

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  type MetricKey = String

  def props(dateTimeParser: DateTimeParser): Props = Props(new LogParserActor(dateTimeParser) with AlertRuleActorCreator)
}

class LogParserActor(dateTimeParser: DateTimeParser) extends Actor with ActorLogging {
  this: AlertRuleActorCreator =>
  private type AlertRuleActorRef = ActorRef

  def receive: Receive = normal(Nil, List.empty, Map())

  def normal(metrics: List[Metric],
             logLines: List[String],
             alertsByMetricKey: Map[MetricKey, List[AlertRuleActorRef]]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender() ! Details(metrics)

    case RequestComponentLogLines =>
      log.debug("received request for loglines")
      sender() ! ComponentLogLines(logLines)

    case RequestParsedLogLines(metricKey) =>
      log.debug("received request for parsed loglines")
      handleGetParsedLogLines(metrics, logLines, metricKey)

    case RequestAlertRules(metricKey) =>
      log.debug("received request for alert rules of {}", metricKey)
      handleGetAlertRules(metrics, alertsByMetricKey, metricKey)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      context.become(normal(metric :: metrics, logLines, alertsByMetricKey))
      sender() ! MetricRegistered(metric)

    case msg: SubmitLog =>
      handleSubmitLog(msg, metrics, alertsByMetricKey)
      context.become(normal(metrics, msg.logLine :: logLines, alertsByMetricKey))

    case msg: DeleteAllAlertingRules =>
      handleDeleteRules(metrics, logLines, alertsByMetricKey, msg)

    case RegisterAlertRule(compId, metricKey, rule) =>
      log.debug("received new alert rule {} in {}", rule, self.path)

      findMetric(metricKey, metrics) match {
        case None => sender() ! MetricNotFound(compId, metricKey)
        case Some(metric) =>
          val newAlertActorRef = create(context, rule, compId, metricKey)
          context.become(normal(metrics, logLines, updateAlertsByMetricKey(alertsByMetricKey, newAlertActorRef, metricKey)))
          sender() ! AlertRuleCreated(compId, metricKey, rule)
      }
  }

  private def findMetric(metricKey: MetricKey, metrics: List[Metric]): Option[Metric] = {
    metrics.find(_.metricKey == metricKey)
  }

  private def updateAlertsByMetricKey(
                                       alertsByMetricKey: Map[MetricKey, List[AlertRuleActorRef]],
                                       newAlertRuleActorRef: AlertRuleActorRef,
                                       key: MetricKey) = {
    alertsByMetricKey + (key -> (newAlertRuleActorRef :: alertsByMetricKey.getOrElse(key, Nil)))
  }


  private def handleSubmitLog(
                               msg: SubmitLog,
                               metrics: List[Metric],
                               alertsByMetricKey: Map[MetricKey, List[AlertRuleActorRef]]) {
    log.debug("received {} in {}", msg, self.path)
    metrics.foreach { metric =>
      val parseResult = parseLogLine(msg.logLine, metric)
      for {
        value <- parseResult.metric
        alertRuleActorRef <- alertsByMetricKey(metric.metricKey)
      } {
        val msg = CheckRuleBreak(value.toType(metric.valueType))
        log.info("sending {} to {}", msg, alertRuleActorRef.path)
        alertRuleActorRef ! msg
      }
    }
  }

  private def handleGetAlertRules(metrics: List[Metric], alertsByMetric: Map[MetricKey, List[ActorRef]], metricKey: String) = {
    findMetric(metricKey, metrics) match {
      case None =>
        sender() ! MetricNotFound
      case Some(_) =>
        alertsByMetric.get(metricKey) match {
          case None =>
            sender() ! AllAlertRuleDetails(Set[AlertRule]())
          case Some(ruleList) =>
            context.actorOf(GetAlertsActor.props(ruleList, sender()))
        }
    }
  }

  private def handleGetParsedLogLines(metrics: List[Metric], logLines: List[String], metricKey: String) = {
    findMetric(metricKey, metrics) match {
      case None =>
        sender() ! MetricNotFound
      case Some(metric) =>
        val lines = logLines.map(line => {
          val pattern: Regex = metric.regex.r
          pattern.findFirstIn(line).getOrElse("")
        })
        sender() ! ComponentLogLines(lines)
    }
  }

  private def handleDeleteRules(metrics: List[Metric],
                                logLines: List[String],
                                alertsByMetric: Map[MetricKey, List[ActorRef]],
                                msg: DeleteAllAlertingRules) = {
    log.debug("received delete request for all rules on metric {}", msg.metricKey)
    findMetric(msg.metricKey, metrics) match {
      case None =>
        sender() ! MetricNotFound(msg.componentId, msg.metricKey)
      case Some(_) =>
        alertsByMetric.get(msg.metricKey) match {
          case None =>
            sender() ! NoAlertsFound(msg.componentId, msg.metricKey)
          case Some(ruleList) =>
            for (ruleActor <- ruleList) {
              context.stop(ruleActor)
            }
            context.become(normal(metrics, logLines, alertsByMetric - msg.metricKey))
            sender() ! AlertRulesDeleted(msg.componentId)
        }
    }
  }

  private def parseLogLine(logLine: String, metric: Metric): ParsedLine = {
    val pattern: Regex = metric.regex.r
    val parser = LineParser(pattern)
    parser.parse(logLine)
  }

}
