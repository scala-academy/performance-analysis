package performanceanalysis.administrator

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpResponse, MessageEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import performanceanalysis.LogParserActor.Details
import performanceanalysis.administrator.AdministratorActor.{GetDetails, GetRegisteredComponents, RegisteredComponents}
import performanceanalysis.{Server, Status, StatusActor}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Jordi on 13-3-2016.
  */
class Administrator extends Server {

  protected lazy val httpPort = adminHttpPort

  protected lazy val httpInterface: String = adminHttpInterface

  protected val statusActor = system.actorOf(StatusActor.props)

  protected val administratorActor = system.actorOf(AdministratorActor.props)

  protected val componentsRoutes = pathPrefix("components") {
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
        ???
      }
  }

  protected val statusRoutes = pathPrefix("status") {
    get {
      log.debug("get /status executed")
      complete((statusActor ? "dummy message").mapTo[Status])
    }
  }

  protected val routes = statusRoutes ~ componentsRoutes

  private def handleGetComponents(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case RegisteredComponents(componentIds) =>
        val entityFuture = Marshal(RegisteredComponents(componentIds)).to[MessageEntity]
        entityFuture.map {
          case registeredComponentsEntity =>
            HttpResponse(
              status = StatusCodes.OK,
              entity = registeredComponentsEntity
            )
        }
    }
  }

  private def handleGetDetails(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case Details(componentId) =>
        val entityFuture = Marshal(Details(componentId)).to[MessageEntity]
        entityFuture.map {
          case registeredComponentsEntity =>
            HttpResponse(
              status = StatusCodes.OK,
              entity = registeredComponentsEntity
            )
        }
    }
  }
}
