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
    case SubmitLogs(componentId, logs) =>
      handleSubmitLogs(logParserActors, componentId, logs, sender)
    case RegisterNewLogParser(componentName, newLogParser) =>
      handleNewLogParser(logParserActors, componentName, newLogParser)
  }

  private def handleSubmitLogs(logParserActors: Map[String, ActorRef], componentId: String, logs: String, sender: ActorRef) = {
    logParserActors.get(componentId) match {
      case None => sender ! LogParserNotFound(componentId)
      case Some(actorRef) => sender ! "OK"
    }
  }

  private def handleNewLogParser(logParserActors: Map[String, ActorRef], componentName: String, newLogParser: ActorRef) = {
    val newLogParserActors = logParserActors.updated(componentName, newLogParser)
    context.become(normal(newLogParserActors))
  }
}
