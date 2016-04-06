package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import performanceanalysis.server.Protocol.{RegisterComponent, _}
import performanceanalysis.server.{Protocol, Server}

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
        complete(handleGet(administratorActor ? GetDetails(componentId)))
      } ~ patch {
        // Handle PATCH of an existing component
        ???
      }
    } ~
      get {
        // Handle GET (get list of all registered components)
        complete(handleGet(administratorActor ? GetRegisteredComponents))
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

  private def handleGet(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case RegisteredComponents(componentIds) =>
        val entityFuture = Marshal(RegisteredComponents(componentIds)).to[ResponseEntity]
        toFutureResponse(entityFuture)
      case Details(componentId) =>
        val entityFuture = Marshal(Details(componentId)).to[ResponseEntity]
        toFutureResponse(entityFuture)
    }
  }

  private def toFutureResponse(entityFuture: Future[ResponseEntity]) = {
    entityFuture.map {
      case registeredComponentsEntity =>
        HttpResponse(
          status = StatusCodes.OK,
          entity = registeredComponentsEntity
        )
    }
  }

}
