package performanceanalysis.administrator

import akka.actor.{ActorContext, ActorLogging, ActorRef}
import performanceanalysis.server.Server

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActorCreater {
  private val actorNamePrefix = "LogParser-"

  def createActorName(componentId: String): String = s"$actorNamePrefix$componentId"
}

trait LogParserActorCreater {
  this: ActorLogging =>

  // TODO: Find proper place for timeout
  implicit val timeout = Server.timeout

  def createLogParserActor(context: ActorContext, componentId: String): ActorRef = {
    context.actorOf(LogParserActor.props, LogParserActorCreater.createActorName(componentId))
  }
}
