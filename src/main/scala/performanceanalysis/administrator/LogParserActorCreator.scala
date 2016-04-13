package performanceanalysis.administrator

import akka.actor.{ActorContext, ActorLogging, ActorRef}
import performanceanalysis.LogParserActor
import performanceanalysis.server.Server

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActorCreator {
  private val actorNamePrefix = "LogParser-"

  def createActorName(componentId: String): String = s"$actorNamePrefix$componentId"
}

trait LogParserActorCreator {
  this: ActorLogging =>

  // TODO: Find proper place for timeout
  implicit val timeout = Server.timeout

  def createLogParserActor(context: ActorContext, componentId: String): ActorRef = {
    context.actorOf(LogParserActor.props, LogParserActorCreator.createActorName(componentId))
  }
}
