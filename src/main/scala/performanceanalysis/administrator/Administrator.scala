package performanceanalysis.administrator

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
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

  private def handleGetComponents: Future[Any] => Future[HttpResponse] = {
    val statusCodeFunction: Any => StatusCode = {
      case RegisteredComponents(componentIds) => StatusCodes.OK
      case _ => StatusCodes.InternalServerError
    }
    val entityFunction: Any => Future[ResponseEntity] = {
      case RegisteredComponents(componentIds) => Marshal(RegisteredComponents(componentIds)).to[ResponseEntity]
      case _ => Future(HttpEntity.Empty)
    }
    createHttpResponse(statusCodeFunction, entityFunction)
  }

  private def handleGetDetails: Future[Any] => Future[HttpResponse] = {
    val statusCodeFunction: Any => StatusCode = {
      case Details(componentId) => StatusCodes.OK
      case _ => StatusCodes.InternalServerError
    }
    val entityFunction: Any => Future[ResponseEntity] = {
      case Details(componentId) => Marshal(Details(componentId)).to[ResponseEntity]
      case _ => Future(HttpEntity.Empty)
    }
    createHttpResponse(statusCodeFunction, entityFunction)
  }

  private def createHttpResponse(statusCodeFunction: Any => StatusCode, entityFunction: Any => Future[ResponseEntity])
                                (resultFuture: Future[Any]): Future[HttpResponse] = {
    for {
      result <- resultFuture
      statusCode = statusCodeFunction(result)
      responseEntity <- entityFunction(result)
    } yield HttpResponse(status = statusCode, entity = responseEntity)
  }
}
