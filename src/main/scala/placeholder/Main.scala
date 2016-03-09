package placeholder

import java.net.InetSocketAddress

import akka.event.{ Logging, LoggingAdapter }
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Main extends App with Server {
  private val log: LoggingAdapter = Logging(system, this.getClass)

  protected val serverPromise = Http().bindAndHandle(routes, httpInterface, httpPort)

  serverPromise.onComplete {
    case Success(serverBinding) => log.info(s"Server ${this.getClass.getName} bound to ${serverBinding.localAddress}")
    case Failure(error)         => log.error(s"Failed to start server: $error")
  }

  def getServerAddress: Future[InetSocketAddress] = serverPromise.map(server => server.localAddress)
}
