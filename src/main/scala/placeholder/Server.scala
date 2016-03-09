package placeholder

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout

trait Server extends Protocol with Config with SprayJsonSupport {
  protected implicit val system: ActorSystem = ActorSystem()
  protected implicit val timeout: Timeout = Timeout(2, TimeUnit.SECONDS)
  private val log: LoggingAdapter = Logging(system, this.getClass)
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  protected val statusActor = system.actorOf(StatusActor.props)

  protected val routes = pathPrefix("status") {
    get {
      log.debug("get /status executed")
      complete((statusActor ? "dummy message").mapTo[Status])
    }
  }
}
