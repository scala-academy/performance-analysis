package performanceanalysis

import akka.actor.{Actor, ActorLogging, Props}
import performanceanalysis.server.Protocol._

import scala.util.matching.Regex

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  def props: Props = Props(new LogParserActor())
}

class LogParserActor extends Actor with ActorLogging {

  def receive: Receive = normal(Nil, Map())

  def normal(metrics: List[Metric], parsedLogs: Map[String, List[Any]]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case LogSubmitted(_, logLine) =>
      log.debug("received log line '{}' to be parsed", logLine)
      parseLogLine(metrics, logLine, parsedLogs)

    case metric: Metric =>
      log.debug(s"received post with metric $metric")
      context.become(normal(metric :: metrics, parsedLogs))
      sender ! MetricRegistered(metric)
  }

  private def findAllMatchedValues(pattern: Regex, logLine: String) = {
    pattern.findAllMatchIn(logLine)
      .filter(_.groupCount >= 1)
      .foldLeft(List[String]())((acc, regMatch) => regMatch.group(1) :: acc)
  }

  private def parseLogLine(metrics: List[Metric], logLine: String, parsedLogs: Map[String, List[Any]]) = {
    metrics foreach { metric =>
      val key = s"${metric.metricKey}-$logLine"
      val pattern: Regex = metric.regex.r
      val list = findAllMatchedValues(pattern, logLine)
      context.become(normal(metrics, parsedLogs.updated(key, list)))
    }
  }
}
