package performanceanalysis.server

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Server {
  protected implicit val system: ActorSystem = ActorSystem()
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(2, TimeUnit.SECONDS)
}

trait Server extends Protocol with Config with SprayJsonSupport {
  protected implicit def system = Server.system

  protected implicit def timeout: Timeout = Server.timeout

  protected implicit def materializer = Server.materializer

  protected def routes: Route = componentsRoute
  protected def componentsRoute: Route

  protected def httpPort: Int

  protected def httpInterface: String

  protected val log: LoggingAdapter = Logging(system, this.getClass)

  protected def serverPromise = Http().bindAndHandle(routes, httpInterface, httpPort)

  serverPromise.onComplete {
    case Success(serverBinding) => log.info(s"Server ${this.getClass.getName} bound to ${serverBinding.localAddress}")
    case Failure(error) => log.error(s"Failed to start server: $error")
  }

  def getServerAddress: Future[InetSocketAddress] = serverPromise.map(server => server.localAddress)
}
