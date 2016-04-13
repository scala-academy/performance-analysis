package performanceanalysis.logreceiver

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import performanceanalysis.server.Server

/**
  * Created by Jordi on 13-3-2016.
  */
class LogReceiver extends Server  {

  protected lazy val httpPort = logReceiverHttpPort

  protected lazy val httpInterface: String = logReceiverHttpInterface

  val logReceiverActor = system.actorOf(LogReceiverActor.props)

  def componentsRoute: Route = pathPrefix("components") {
    get {
      log.debug("get /components executed")
      complete("dummy response")
    }
  }

  override def routes: Route = componentsRoute
}
