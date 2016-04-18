package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
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
    path(Segment / "metrics") { componentId =>
      get {
        // Handle GET of an existing component to obtain metrics only
        complete(handleGet(administratorActor ? GetDetails(componentId)))
      }
    } ~
      path(Segment) { componentId =>
        get {
          // Handle GET of an existing component
          complete(handleGet(administratorActor ? GetDetails(componentId)))
        } ~ post {
          // Handle POST of an existing component
          entity(as[Metric]) { metric =>
            log.debug(s"Received POST on /components/$componentId with entity $metric")
            complete(handlePost(administratorActor ? RegisterMetric(componentId, metric)))
          }
        } ~
          patch {
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
          complete(handlePost(administratorActor ? registerComponent))
        }
      }
  }

  protected def routes: Route = componentsRoute

  private def handlePost(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case LogParserCreated(componentId) =>
        Future(HttpResponse(status = StatusCodes.Created))
      case LogParserExisted(componentId) =>
        ???
      case MetricRegistered(metric) =>
        Future(HttpResponse(status = StatusCodes.Created))
    }
  }

  private def handleGet(resultFuture: Future[Any]): Future[HttpResponse] = {
    def toFutureResponse(entityFuture: Future[ResponseEntity], status: StatusCode) = {
      entityFuture.map {
        case registeredComponentsEntity =>
          HttpResponse(status).withEntity(registeredComponentsEntity)
      }
    }

    resultFuture.flatMap {
      case RegisteredComponents(componentIds) =>
        val entityFuture = Marshal(RegisteredComponents(componentIds)).to[ResponseEntity]
        toFutureResponse(entityFuture, StatusCodes.OK)
      case Details(metrics) =>
        val entityFuture = Marshal(Details(metrics)).to[ResponseEntity]
        toFutureResponse(entityFuture, StatusCodes.OK)
    }
  }

}
