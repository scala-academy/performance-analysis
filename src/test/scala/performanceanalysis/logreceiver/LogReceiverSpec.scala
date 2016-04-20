package performanceanalysis.logreceiver

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import performanceanalysis.base.SpecBase

/**
  * Created by Jordi on 5-4-2016.
  */
class LogReceiverSpec extends SpecBase with ScalatestRouteTest {
  "The log receiver" must {
    "handle a GET on /components response with status code 405" in new LogReceiver {
      Put() ~> Route.seal(routes) ~> check {
        status === StatusCodes.MethodNotAllowed
      }
    }
  }
}