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

  def receive: Receive = normal(Map.empty[String, ActorRef])

  def normal(logParserActors: Map[String, ActorRef]): Receive = {
    case SubmitLog(componentId, logLine) =>
      handleSubmitLog(logParserActors, componentId, logLine, sender)
    case RegisterNewLogParser(componentName, newLogParser) =>
      handleNewLogParser(logParserActors, componentName, newLogParser)
  }

  private def handleSubmitLog(logParserActors: Map[String, ActorRef], componentId: String, logLine: String, sender: ActorRef) = {
    logParserActors.get(componentId) match {
      case None => sender ! LogParserNotFound(componentId)
      case Some(logParserActor) =>
        logParserActor ! SubmitLog(componentId, logLine) // eventually log line will be parsed
        sender ! LogSubmitted(componentId, logLine)
    }
  }

  private def handleNewLogParser(logParserActors: Map[String, ActorRef], componentName: String, newLogParser: ActorRef) = {
    val newLogParserActors = logParserActors.updated(componentName, newLogParser)
    context.become(normal(newLogParserActors))
  }
}
