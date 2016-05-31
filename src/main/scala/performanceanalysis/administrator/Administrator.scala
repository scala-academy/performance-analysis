package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.StatusCodes.{Created, NotFound}
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import performanceanalysis.server.Protocol.Rules.AlertRule
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

  override protected def componentsRoute: Route = pathPrefix("components") {
    pathPrefix(Segment) { componentId =>
      pathPrefix("metrics") {
        pathEnd {
          post {
            entity(as[Metric]) { metric =>
              log.debug(s"Received POST on /components/$componentId with entity $metric")
              complete(handlePost(administratorActor ? RegisterMetric(componentId, metric)))
            }
          }
        } ~ pathPrefix(Segment) { metricKey =>
          path("alerting-rules") {
            post {
              entity(as[AlertRule]) { rule =>
                log.debug(s"Received POST for new rule: $rule for $componentId/$metricKey")
                complete(handlePost(administratorActor ? RegisterAlertRule(componentId, metricKey, rule)))
              }
            } ~ get {
              // Handle GET of an existing component
              complete(handleGet(administratorActor ? GetAlertRules(componentId, metricKey)))
            } ~ delete {
              // Delete all registered alert rules
              log.debug("Received DELETE for all rules for {}/{}", componentId, metricKey)
              complete(handleDelete(administratorActor ? DeleteAllAlertingRules(componentId, metricKey)))
            }
          }
        }
      } ~ path("logs") {
        get {
          log.debug(s"Received GET for loglines for $componentId")
          complete(handleGet(administratorActor ? GetComponentLogLines(componentId)))
        }
      } ~ get {
        // Handle GET of an existing component to obtain metrics only
        complete(handleGet(administratorActor ? GetDetails(componentId)))
      }
    } ~ get {
        // Handle GET (get list of all registered components)
        complete(handleGet(administratorActor ? GetRegisteredComponents))
    } ~ post {
        // Handle POST (registration of a new component)
        entity(as[RegisterComponent]) { (registerComponent: RegisterComponent) =>
          log.debug(s"Received POST on /components with entity $registerComponent")
          complete(handlePost(administratorActor ? registerComponent))
        }
    }
  }

  private def handlePost(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case LogParserCreated(componentId) =>
        Future(HttpResponse(status = Created))
      case LogParserExisted(componentId) =>
        ???
      case MetricRegistered(metric) =>
        Future(HttpResponse(status = Created))
      case msg:AlertRuleCreated =>
        Future(HttpResponse(status = Created))
      case msg: MetricNotFound =>
        Future(HttpResponse(status = NotFound))
    }
  }

  private def handleDelete(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case AlertRulesDeleted(componentId) =>
        log.debug("Successful delete of alert rules for {}", componentId)
        Future(HttpResponse(status = StatusCodes.NoContent))
      case msg: MetricNotFound =>
        Future(HttpResponse(status = NotFound))
      case NoAlertsFound(_, _) =>
        Future(HttpResponse(status = StatusCodes.NoContent))
    }
  }

  private def handleGet(resultFuture: Future[Any]): Future[HttpResponse] = {
    def toFutureResponse(entityFuture: Future[ResponseEntity], status: StatusCode) = {
      entityFuture.map {
        case entity =>
          HttpResponse(status).withEntity(entity)
      }
    }

    resultFuture.flatMap {
      case RegisteredComponents(componentIds) =>
        val entityFuture = Marshal(RegisteredComponents(componentIds)).to[ResponseEntity]
        toFutureResponse(entityFuture, StatusCodes.OK)
      case Details(metrics) =>
        val entityFuture = Marshal(Details(metrics)).to[ResponseEntity]
        toFutureResponse(entityFuture, StatusCodes.OK)
      case ComponentLogLines(logLines) =>
        val entityFuture = Marshal(logLines).to[ResponseEntity]
        toFutureResponse(entityFuture, StatusCodes.OK)
      case msg:AllAlertRuleDetails =>
        val entityFuture = Marshal(msg).to[ResponseEntity]
        toFutureResponse(entityFuture, StatusCodes.OK)
      case msg:MetricNotFound =>
        Future(HttpResponse(status = NotFound))
    }
  }

}
