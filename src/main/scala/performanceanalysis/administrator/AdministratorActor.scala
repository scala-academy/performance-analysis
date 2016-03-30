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

  // TODO: Find proper place for timeout
  override implicit val timeout = Server.timeout

  def receive: Receive = {
    case RegisterComponent(componentId) =>
      handleRegisterComponent(componentId, sender)
    case GetDetails(componentId) =>
      handleGetDetails(componentId, sender)
    case GetRegisteredComponents =>
      ???
  }

  private def handleRegisterComponent(componentId: String, sender: ActorRef) = {
    findLogParserActor(context, componentId).map(_ match {
      case None =>
        val newActor = createLogParserActor(context, componentId)
        log.debug(s"Actor created with path ${newActor.path}")
        sender ! LogParserCreated(componentId)
      case Some(ref) =>
        log.debug(s"Actor with component $componentId already existed")
        sender ! LogParserExisted(componentId)
    })
  }

  private def handleGetDetails(componentId: String, sender: ActorRef) = {
    findLogParserActor(context, componentId).map(_ match {
      case None => sender ! LogParserNotFound(componentId)
      case Some(ref) =>
        log.debug(s"Requesting details from ${ref.path}")
        (ref ? RequestDetails) pipeTo sender
    })
  }
}


