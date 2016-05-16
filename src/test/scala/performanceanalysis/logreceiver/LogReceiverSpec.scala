package performanceanalysis.logreceiver

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import performanceanalysis.base.SpecBase

class LogReceiverSpec extends SpecBase with ScalatestRouteTest {
  "The log receiver" must {
    "handle a POST on /components response with status code 405" in new LogReceiver {
      Post() ~> Route.seal(routes) ~> check {
        status === StatusCodes.Accepted
      }
    }

    "handle a GET on /components response with status code 405" in new LogReceiver {
      Get() ~> Route.seal(routes) ~> check {
        status === StatusCodes.MethodNotAllowed
      }
    }
  }
}