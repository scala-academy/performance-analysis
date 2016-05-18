package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.StatusCodes.{Created, NotFound}
import akka.http.scaladsl.model.{HttpResponse, ResponseEntity, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import performanceanalysis.server.InterActorMessage.Details
import performanceanalysis.server.Protocol.Rules.AlertingRule
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
    pathPrefix(Segment) { componentId =>
      get {
        // Handle GET of an existing component to obtain metrics only
        complete(handleGet(administratorActor ? GetDetails(componentId)))

      } ~ pathPrefix("metrics") {

            post {
              pathEnd {
                entity(as[Metric]) { metric =>

                  log.debug(s"Received POST on /components/$componentId with entity $metric")
                  complete(handlePost(administratorActor ? RegisterMetric(componentId, metric)))
                }
              } ~ pathPrefix(Segment) { metricKey =>

                    path("alerting-rules") {
                      entity(as[AlertingRule]) { rule =>
                        log.debug(s"Received POST for new rule: $rule for $componentId/$metricKey")
                        complete(handlePost(administratorActor ? RegisterAlertingRule(componentId, metricKey, rule)))
                      }
                    }
                  }
            } ~ get {
                  // Handle GET of an existing component
                  complete(handleGet(administratorActor ? GetDetails(componentId)))
                }
          }
    } ~
      get {
        // Handle GET (get list of all registered components)
        complete(handleGet(administratorActor ? GetRegisteredComponents))
      } ~
      post {
        // Handle POST (registration of a new component)
        entity(as[RegisterComponent]) { (registerComponent: RegisterComponent) =>
          log.debug(s"Received POST on /components with entity $registerComponent")
          complete(handlePost(administratorActor ? registerComponent))
        }
      }
  }

  override protected def routes: Route = componentsRoute

  private def handlePost(resultFuture: Future[Any]): Future[HttpResponse] = {
    resultFuture.flatMap {
      case LogParserCreated(componentId) =>
        Future(HttpResponse(status = Created))
      case LogParserExisted(componentId) =>
        ???
      case MetricRegistered(metric) =>
        Future(HttpResponse(status = Created))
      case msg:AlertingRuleCreated =>
        Future(HttpResponse(status = Created))
      case msg: MetricNotFound =>
        Future(HttpResponse(status = NotFound))
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
