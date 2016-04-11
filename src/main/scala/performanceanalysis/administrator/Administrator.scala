package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}
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
        complete(respondWhenAvailable(administratorActor ? GetDetails(componentId)))
      } ~ patch {
        // Handle PATCH of an existing component
        complete(HttpResponse(status = StatusCodes.NotImplemented))
      }
    } ~
      get {
        // Handle GET (get list of all registered components)
        complete(respondWhenAvailable(administratorActor ? GetRegisteredComponents))
      } ~
      post {
        // Handle POST (registration of a new component)
        entity(as[RegisterComponent]) { registerComponent =>
          log.debug(s"Received POST on /components with entity $registerComponent")
          complete(HttpResponse(status = StatusCodes.NotImplemented))
        }
      }
  }

  protected def routes: Route = componentsRoute

  private def respondWhenAvailable(future: Future[Any]): Future[HttpResponse] = {
    future.flatMap {
      case RegisteredComponents(componentId) =>
        toResponse(Marshal(RegisteredComponents(componentId)).to[ResponseEntity])
      case Details(componentId) =>
        toResponse(Marshal(Details(componentId)).to[ResponseEntity])
    }
  }

  private def toResponse(future: Future[ResponseEntity]):Future[HttpResponse] = {
    future.map {
      case responseEntity => HttpResponse(status = StatusCodes.OK, entity = responseEntity)
    }
  }
}
