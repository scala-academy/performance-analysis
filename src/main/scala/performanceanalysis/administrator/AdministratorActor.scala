package performanceanalysis.administrator

import akka.actor._
import akka.pattern.{ask, pipe}
import performanceanalysis.server.Protocol._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by m06f791 on 24-3-2016.
  */

object AdministratorActor {

  def props(logReceiverActor: ActorRef): Props = Props(new AdministratorActor(logReceiverActor))
}

class AdministratorActor(logReceiverActor: ActorRef) extends Actor with ActorLogging with LogParserActorCreator {
  this: LogParserActorCreator =>

  def receive: Receive = normal(Map.empty[String, ActorRef])

  def normal(logParserActors: Map[String, ActorRef]): Receive = {
    case RegisterComponent(componentId) =>
      handleRegisterComponent(logParserActors, componentId)
    case GetDetails(componentId) =>
      handleGetDetails(logParserActors, componentId)
    case GetRegisteredComponents =>
      handleRegisteredComponents(logParserActors)
  }

  private def handleRegisteredComponents(logParserActors: Map[String, ActorRef]) = {
    val registeredComponents: Set[String] = logParserActors.keySet
    log.debug(s"Returning all registered components $registeredComponents")
    sender ! registeredComponents
  }

  private def handleRegisterComponent(logParserActors: Map[String, ActorRef], componentId: String) = {
    logParserActors.get(componentId) match {
      case None =>
        val newActor = createLogParserActor(context, componentId)
        // Notify LogReceiver of new actor
        logReceiverActor ! RegisterNewLogParser(componentId, newActor)
        // Update actor state
        val newLogParserActors = logParserActors.updated(componentId, newActor)
        context.become(normal(newLogParserActors))
        // Respond to sender
        sender ! LogParserCreated(componentId)
      case Some(ref) =>
        log.debug(s"Actor with component $componentId already existed")
        sender ! LogParserExisted(componentId)
    }
  }

  private def handleGetDetails(logParserActors: Map[String, ActorRef], componentId: String) = {
    logParserActors.get(componentId) match {
      case None => sender ! LogParserNotFound(componentId)
      case Some(ref) =>
        log.debug(s"Requesting details from ${ref.path}")
        (ref ? RequestDetails) pipeTo sender
    }
  }
}


