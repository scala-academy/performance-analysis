package performanceanalysis.logreceiver

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import performanceanalysis.server.Server
import performanceanalysis.server.messages.AdministratorMessages.LogParserNotFound
import performanceanalysis.server.messages.LogMessages._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Jordi on 13-3-2016.
  */
class LogReceiver extends Server  {

  protected lazy val httpPort = logReceiverHttpPort

  protected lazy val httpInterface: String = logReceiverHttpInterface

  val logReceiverActor = system.actorOf(LogReceiverActor.props)

  override protected def routes: Route = pathSingleSlash {
    get {
      complete(MethodNotAllowed, None)
    }
  } ~ pathPrefix("components") {
    get {
      log.debug("get /components executed")
      complete("dummy response")
    } ~
    pathPrefix(Segment) { componentId =>
      path("logs") {
        post {
          entity(as[Log]) { log =>
            complete(handlePostLog(logReceiverActor ? SubmitLogs(componentId, log.logLines)))
          }
        }
      }
    }
  }

  private def handlePostLog(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case LogParserNotFound(componentId) => Future(HttpResponse(status = NotFound))
      case LogsSubmitted => Future(HttpResponse(status = Accepted))
    }
  }
}
