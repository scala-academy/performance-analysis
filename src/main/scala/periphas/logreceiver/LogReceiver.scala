package periphas.logreceiver

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import periphas.server.Protocol._
import periphas.server.Server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Jordi on 13-3-2016.
  */
class LogReceiver extends Server  {

  protected lazy val httpPort = logReceiverHttpPort

  protected lazy val httpInterface: String = logReceiverHttpInterface

  val logReceiverActor = system.actorOf(LogReceiverActor.props)

  def componentsRoute: Route = pathSingleSlash {
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
          entity(as[String]) { logLine =>
            complete(handlePostLog(logReceiverActor ? SubmitLogs(componentId = componentId, logs = logLine)))
          }
        }
      }
    }
  }

  override def routes: Route = componentsRoute

  private def handlePostLog(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case LogParserNotFound(componentId) =>
        Future(HttpResponse(status = NotFound))
      case "OK" => Future(HttpResponse(status = OK))
    }
  }
}
