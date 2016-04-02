package performanceanalysis.administrator

import akka.actor._
import akka.pattern.{ask, pipe}
import performanceanalysis.LogParserActor.RequestDetails
import performanceanalysis.Server
import performanceanalysis.administrator.AdministratorActor._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by m06f791 on 24-3-2016.
  */

object AdministratorActor {

  case class RegisterComponent(componentId: String)

  case class LogParserCreated(componentId: String)

  case class LogParserExisted(componentId: String)

  case class LogParserNotFound(componentId: String)

  case class GetDetails(componentId: String)

  case object GetRegisteredComponents

  case class RegisteredComponents(componentIds: Set[String])

  def props: Props = Props(new AdministratorActor)
}

class AdministratorActor extends Actor with ActorLogging with LogParserActorManager {
  this: LogParserActorManager =>

  def receive: Receive = normal(Map.empty[String, ActorRef])

  def normal(logParserActors: Map[String, ActorRef]): Receive = {
    case RegisterComponent(componentId) =>
      handleRegisterComponent(logParserActors, componentId, sender)
    case GetDetails(componentId) =>
      handleGetDetails(logParserActors, componentId, sender)
    case GetRegisteredComponents =>
      ???
  }

  private def handleRegisterComponent(logParserActors: Map[String, ActorRef], componentId: String, sender: ActorRef) = {
    findLogParserActor(logParserActors, componentId) match {
      case None =>
        val newActor = createLogParserActor(context, componentId)
        log.debug(s"Actor created with path ${newActor.path}")
        sender ! LogParserCreated(componentId)
        val newLogParserActors = logParserActors.updated(componentId, newActor)
        context.become(normal(newLogParserActors))
      case Some(ref) =>
        log.debug(s"Actor with component $componentId already existed")
        sender ! LogParserExisted(componentId)
    }
  }

  private def handleGetDetails(logParserActors: Map[String, ActorRef], componentId: String, sender: ActorRef) = {
    findLogParserActor(logParserActors, componentId) match {
      case None => sender ! LogParserNotFound(componentId)
      case Some(ref) =>
        log.debug(s"Requesting details from ${ref.path}")
        (ref ? RequestDetails) pipeTo sender
    }
  }
}


