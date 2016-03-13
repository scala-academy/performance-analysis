package placeholder

import akka.http.scaladsl.server.Directives._

/**
  * Created by Jordi on 13-3-2016.
  */
trait DataGatherer extends Server {

  protected lazy val httpPort = gathererHttpPort

  protected lazy val httpInterface: String = gathererHttpInterface

  protected val routes = pathPrefix("data") {
    get {
      log.debug("get /data executed")
      complete("Data!")
    }
  }
}
