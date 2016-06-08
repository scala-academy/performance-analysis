package performanceanalysis.administrator

import akka.actor.{ActorContext, ActorLogging, ActorRef}
import performanceanalysis.server.Server
import performanceanalysis.{DateTimeParser, LogParserActor}

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

  def createLogParserActor(context: ActorContext, componentId: String, dateFormat: Option[String]): ActorRef = {
    val parser = DateTimeParser.parser(dateFormat)
    context.actorOf(LogParserActor.props(DateTimeParser.mdy), LogParserActorCreater.createActorName(componentId))
  }
}
