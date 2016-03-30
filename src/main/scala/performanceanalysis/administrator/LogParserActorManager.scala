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

  def findLogParserActor(context: ActorContext, componentId: String): Future[Option[ActorRef]] = {
    val searchString = s"${LogParserActorManager.createActorName(componentId)}"
    val actorSearch = context.actorSelection(searchString).resolveOne()
    actorSearch.map { ref =>
      log.debug(s"""Actor $componentId found with search "$searchString" from ${context.self.path}""")
      Some(ref)
    }.recover { case _: Throwable =>
      log.debug(s"""Actor $componentId not found with search "$searchString" from ${context.self.path}""")
      None
    }
  }
}