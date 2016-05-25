package performanceanalysis.logreceiver

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol._

import scala.collection.mutable
import LogReceiverActor.splitIntoLines

/**
  * Created by Jordi on 5-4-2016.
  */

object LogReceiverActor {
  def props: Props = Props(new LogReceiverActor)

  /**
    * This function returns an array of lines. It treats
    * \r\n, \n, \r as a new line
    * @param input to be split
    * @return array of lines
    */
  def splitIntoLines(input: String):List[String] = {
    input.split("[\r\n]+").toList
  }
}

class LogReceiverActor extends Actor with ActorLogging {
  private type ComponentId = String
  private type LogParser = ActorRef
  private val logParsersById: mutable.Map[ComponentId, LogParser] = mutable.Map()

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
        splitIntoLines(logLines) foreach { logLine => logParser ! SubmitLog(compId, logLine)}
        sender() ! LogsSubmitted
    }
  }
}
