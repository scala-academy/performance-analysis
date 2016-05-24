package performanceanalysis.logreceiver

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol._
import performanceanalysis.util.Utils.splitIntoLines

/**
  * Created by Jordi on 5-4-2016.
  */

object LogReceiverActor {
  def props: Props = Props(new LogReceiverActor)
}

class LogReceiverActor extends Actor with ActorLogging {
  private type ComponentId = String
  private type LogParser = ActorRef
  private var logParsersById: Map[ComponentId, LogParser] = Map()

  def receive: Receive = {
    case SubmitLogs(componentId, logLines) =>
      log.debug("Logs submitted with {}", componentId)
      handleSubmitLog(componentId, logLines)
    case RegisterNewLogParser(compId, newLogParser) =>
      log.debug("New LogParser created with {}", compId)
      logParsersById += compId -> newLogParser
  }

  private def handleSubmitLog(compId: ComponentId, logLines: String) = {
    logParsersById.get(compId) match {
      case None => sender() ! LogParserNotFound(compId)
      case Some(logParser) =>
        log.info("Log lines {}", splitIntoLines(logLines))
        splitIntoLines(logLines) foreach { logLine => logParser ! SubmitLog(compId, logLine)}
        sender() ! LogsSubmitted
    }
  }
}
