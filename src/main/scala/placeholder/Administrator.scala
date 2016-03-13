package placeholder

import akka.http.scaladsl.server.Directives._
import akka.pattern.ask

/**
  * Created by Jordi on 13-3-2016.
  */
class Administrator extends Server {

  protected lazy val httpPort = adminHttpPort

  protected lazy val httpInterface: String = adminHttpInterface

  protected val statusActor = system.actorOf(StatusActor.props)

  protected val routes = pathPrefix("status") {
    get {
      log.debug("get /status executed")
      complete((statusActor ? "dummy message").mapTo[Status])
    }
  }
}
