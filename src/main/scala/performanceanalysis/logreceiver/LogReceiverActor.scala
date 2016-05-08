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
  private type LogParsers = Map[ComponentId, LogParser]

  def receive: Receive = normal(Map())

  def normal(logParsers: LogParsers): Receive = {
    case msg: SubmitLog =>
      handleSubmitLog(logParsers, msg)
    case RegisterNewLogParser(compId, newLogParser) =>
      log.info(s"New LogParser created with $compId")
      handleNewLogParser(logParsers, compId, newLogParser)
  }

  private def handleSubmitLog(logParsers: LogParsers, msg: SubmitLog) = {
    logParsers.get(msg.componentId) match {
      case None => sender ! LogParserNotFound(msg.componentId)
      case Some(logParser) =>
        logParser ! msg // eventually log line will be parsed
        sender() ! LogSubmitted
    }
  }

  private def handleNewLogParser(logParsers: LogParsers, compId: ComponentId, newParser: LogParser) = {
    val newLogParserActors = logParsers.updated(compId, newParser)
    context.become(normal(newLogParserActors))
  }
}
