package performanceanalysis.logreceiver

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.messages.AdministratorMessages.LogParserNotFound
import performanceanalysis.server.messages.LogMessages._

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
    *
    * @param input to be split
    * @return list of lines
    */
  def splitIntoLines(input: String): List[String] = {
    input.split("[\r\n]+").toList
  }
}

class LogReceiverActor extends Actor with ActorLogging {
  private type ComponentId = String
  private type LogParser = ActorRef
  private val logParsersById: mutable.Map[ComponentId, LogParser] = mutable.Map()

  def receive: Receive = {
    case SubmitLogs(componentId, logLines) => handleSubmitLogs(componentId, logLines)
    case RegisterNewLogParser(compId, newLogParser, dateFormat) =>
      log.info("New LogParser created with {}", compId)
      handleRegisterNewLogParser(compId, newLogParser)
  }

  private def handleSubmitLogs(compId: ComponentId, logLines: String) = {
    logParsersById.get(compId) match {
      case None => sender() ! LogParserNotFound(compId)
      case Some(logParser) =>
        splitIntoLines(logLines) foreach { logLine => logParser ! SubmitLog(compId, logLine)}
        sender() ! LogsSubmitted
    }
  }

  private def handleRegisterNewLogParser(compId: ComponentId, newParser: LogParser) = {
    logParsersById += compId -> newParser
  }
}
