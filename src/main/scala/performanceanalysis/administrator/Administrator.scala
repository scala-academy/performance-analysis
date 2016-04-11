package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.StatusCodes.NotImplemented
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import performanceanalysis.server.Protocol.{RegisterComponent, _}
import performanceanalysis.server.Server

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Jordi on 13-3-2016.
  */
class Administrator(logReceiverActor: ActorRef) extends Server {

  protected lazy val httpPort = adminHttpPort

  protected lazy val httpInterface: String = adminHttpInterface

  protected val administratorActor = system.actorOf(AdministratorActor.props(logReceiverActor))

  def componentsRoute: Route = pathPrefix("components") {
    path(Segment) { componentId =>
      get {
        // Handle GET of an existing component
        complete(handleGetDetails(administratorActor ? GetDetails(componentId)))
      } ~ patch {
        // Handle PATCH of an existing component
        ???
      }
    } ~
      get {
        // Handle GET (get list of all registered components)
        complete(handleGetComponents(administratorActor ? GetRegisteredComponents))
      } ~
      post {
        // Handle POST (registration of a new component)
        entity(as[RegisterComponent]) { registerComponent =>
          log.debug(s"Received POST on /components with entity $registerComponent")
          complete(HttpResponse(status = NotImplemented))
        }
      }
  }

  protected def routes: Route = componentsRoute

  private def handleGetComponents(future: Future[Any]): Future[HttpResponse] = {
    new FutureWrapper(future) toResponse {case RegisteredComponents(componentIds) =>
      Marshal(RegisteredComponents(componentIds)).to[ResponseEntity]}
  }

  private def handleGetDetails(resultFuture: Future[Any]): Future[HttpResponse] = {
    new FutureWrapper(resultFuture) toResponse {case Details(componentId) =>
      Marshal(Details(componentId)).to[ResponseEntity]}
  }


  class FutureWrapper(val future: Future[Any], val httpStatus: StatusCode = StatusCodes.OK) {

    def toResponse(transform: Function[Any, Future[ResponseEntity]]): Future[HttpResponse] = {
      future.flatMap {
        value =>
          val entityFuture: Future[ResponseEntity] = transform.apply(value)
          entityFuture.map {
            case responseEntity =>
              HttpResponse(
                status = httpStatus,
                entity = responseEntity
              )

          }
      }

    }
  }
}
