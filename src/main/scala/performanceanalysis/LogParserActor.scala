package performanceanalysis

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.Rules.AlertRule
import performanceanalysis.server.Protocol.{AlertRuleCreated, CheckRuleBreak, _}

import scala.util.matching.Regex
import scala.collection.mutable
import scala.collection.mutable.{Map => MutableMap}


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

  private val metrics = mutable.MutableList[Metric]()
  private val logLines = mutable.MutableList[String]()
  private val alertsByMetricKey = MutableMap[MetricKey, List[AlertRuleActorRef]]()

  def receive: Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender() ! Details(metrics.toList)

    case RequestComponentLogLines =>
      log.debug("received request for loglines")
      sender() ! ComponentLogLines(logLines.toList)

    case RequestParsedLogLines(metricKey) =>
      log.debug("received request for parsed loglines")
      handleGetParsedLogLines(metricKey)

    case RequestAlertRules(metricKey) =>
      log.debug("received request for alert rules of {}", metricKey)
      handleGetAlertRules(metricKey)

    case metric: Metric =>
      log.debug("received post with metric {}", metric)
      metrics += metric
      sender() ! MetricRegistered(metric)

    case msg: SubmitLog =>
      handleSubmitLog(msg)

    case msg: DeleteAllAlertingRules =>
      handleDeleteRules(msg)

    case RegisterAlertRule(compId, metricKey, rule) =>
      handleRegisterAlertRule(compId, metricKey, rule)
  }

  private def handleRegisterAlertRule(compId: String, metricKey: String, rule: AlertRule): Unit = {
    log.debug("received new alert rule {} in {}", rule, self.path)

    findMetric(metricKey) match {
      case None => sender() ! MetricNotFound(compId, metricKey)
      case Some(metric) =>
        val newAlertActorRef = create(context, rule, compId, metricKey)
        updateAlertsByMetricKey(newAlertActorRef, metricKey)
        sender() ! AlertRuleCreated(compId, metricKey, rule)
    }
  }

  private def findMetric(metricKey: MetricKey):Option[Metric] = {
    metrics.find(_.metricKey == metricKey)
  }

  private def updateAlertsByMetricKey(newAlertRuleActorRef: AlertRuleActorRef,
                                      key: MetricKey) = {
    alertsByMetricKey += (key -> (newAlertRuleActorRef :: alertsByMetricKey.getOrElse(key, Nil)))
  }

  private def handleSubmitLog(msg: SubmitLog) {
    log.debug("received {} in {}", msg, self.path)
    logLines += msg.logLine
    for {
      value <- parseResult.metric
      alertRuleActorRef <- alertsByMetricKey(metric.metricKey)
    } {
      val msg = CheckRuleBreak(value.toType(metric.valueType))
      log.info("sending {} to {}", msg, alertRuleActorRef.path)
      alertRuleActorRef ! msg
    }
  }

  private def handleGetAlertRules(metricKey: String) = {
    findMetric(metricKey) match {
      case None =>
        sender() ! MetricNotFound
      case Some(_) =>
        alertsByMetricKey.get(metricKey) match {
          case None =>
            sender() ! AllAlertRuleDetails(Set[AlertRule]())
          case Some(ruleList) =>
            context.actorOf(GetAlertsActor.props(ruleList, sender()))
        }
    }
  }

  private def handleGetParsedLogLines(metricKey: String) = {
    findMetric(metricKey) match {
      case None =>
        sender() ! MetricNotFound
      case Some(metric) =>
        val lines = logLines.map(line => {
          val pattern: Regex = metric.regex.r
          pattern.findFirstIn(line).getOrElse("")
        })
        sender() ! ComponentLogLines(lines.toList)
    }
  }

  private def handleDeleteRules(msg: DeleteAllAlertingRules) = {  log.debug("received delete request for all rules on metric {}", msg.metricKey)
    findMetric(msg.metricKey) match {
      case None =>
        sender() ! MetricNotFound(msg.componentId, msg.metricKey)
      case Some(_) =>
        alertsByMetricKey.get(msg.metricKey) match {
          case None =>
            sender() ! NoAlertsFound(msg.componentId, msg.metricKey)
          case Some(ruleList) =>
            for (ruleActor <- ruleList) {
              context.stop(ruleActor)
            }

            alertsByMetricKey -= msg.metricKey
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
