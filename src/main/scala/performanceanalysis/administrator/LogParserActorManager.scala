package performanceanalysis.administrator

import akka.actor.{ActorContext, ActorLogging, ActorRef}
import performanceanalysis.{LogParserActor, Server}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActorManager {
  private val actorNamePrefix = "LogParser-"

  def createActorName(componentId: String): String = s"$actorNamePrefix$componentId"
}

trait LogParserActorManager {
  this: ActorLogging =>

  // TODO: Find proper place for timeout
  implicit val timeout = Server.timeout

  def createLogParserActor(context: ActorContext, componentId: String): ActorRef = {
    context.actorOf(LogParserActor.props, LogParserActorManager.createActorName(componentId))
  }

  def findLogParserActor(logParserActors: Map[String, ActorRef], componentId: String): Option[ActorRef] = logParserActors.get(componentId)
}