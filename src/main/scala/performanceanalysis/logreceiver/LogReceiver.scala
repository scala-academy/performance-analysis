package performanceanalysis.logreceiver

import akka.http.scaladsl.server.Directives._
import performanceanalysis.server.Server

/**
  * Created by Jordi on 13-3-2016.
  */
class LogReceiver extends Server  {

  protected lazy val httpPort = logReceiverHttpPort

  protected lazy val httpInterface: String = logReceiverHttpInterface

  val logReceiverActor = system.actorOf(LogReceiverActor.props)

  protected val routes = pathPrefix("components") {
    get {
      log.debug("get /components executed")
      complete("dummy response")
    }
  }
}
