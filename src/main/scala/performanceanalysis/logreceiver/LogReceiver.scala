package performanceanalysis.logreceiver

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask

import performanceanalysis.server.Protocol.{MetricRegistered, _}
import performanceanalysis.server.Server

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
      complete(StatusCodes.MethodNotAllowed, None)
    }
  } ~ pathPrefix("components") {
    get {
      log.debug("get /components executed")
      complete("dummy response")
    } ~ path(Segment) { componentId =>
      post {
        // Handle POST of an existing component
        entity(as[String]) { logline =>
          log.debug(s"Received POST on /components/$componentId with logline $logline")
          complete(handlePost(logReceiverActor ? SubmitLog(componentId, logline)))
        }
      }
    }
  }

  override def routes: Route = componentsRoute

  private def handlePost(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case LogSubmitted(componentId, logs) =>
        Future(HttpResponse(status = StatusCodes.Accepted))
      case LogParserNotFound(componentId) =>
        ???
    }
  }

}
