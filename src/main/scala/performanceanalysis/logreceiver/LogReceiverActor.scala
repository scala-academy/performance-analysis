package performanceanalysis.logreceiver

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import performanceanalysis.server.Protocol._

/**
  * Created by Jordi on 5-4-2016.
  */

object LogReceiverActor {

  def props: Props = Props(new LogReceiverActor)
}

class LogReceiverActor extends Actor with ActorLogging {
  private type ComponentId = String
  private type LogParser = ActorRef

  def receive: Receive = normal(Map())

  def normal(logParsersById: Map[ComponentId, LogParser]): Receive = {
    case msg: SubmitLog =>
      handleSubmitLog(logParsersById, msg)
    case RegisterNewLogParser(compId, newLogParser, dateFormat) =>
      log.info("New LogParser created with {}", compId)
      handleRegisterNewLogParser(logParsersById, compId, newLogParser)
  }

  private def handleSubmitLog(logParsersById: Map[ComponentId, LogParser], msg: SubmitLog) = {
    logParsersById.get(msg.componentId) match {
      case None => sender() ! LogParserNotFound(msg.componentId)
      case Some(logParser) =>
        logParser ! msg // eventually log line will be parsed
        sender() ! LogSubmitted
    }
  }

  private def handleRegisterNewLogParser(logParsers: Map[ComponentId, LogParser],
                                         compId: ComponentId, newParser: LogParser) = {
    val newLogParserActors = logParsers.updated(compId, newParser)
    context.become(normal(newLogParserActors))
  }
}
