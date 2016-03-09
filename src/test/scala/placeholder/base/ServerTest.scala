package placeholder.base

import akka.http.scaladsl.testkit.ScalatestRouteTest
import placeholder.{ Server, Status }

/**
 * Created by Jordi on 9-3-2016.
 */
class ServerTest extends SpecBase with ScalatestRouteTest {

  "The server" must {
    "handle a GET on /status and return a positive uptime in milliseconds" in new Server {
      Get("/status") ~> routes ~> check {
        val status = responseAs[Status]
        val uptime = status.uptime
        val (time, millisStr) = uptime.splitAt(uptime.indexOf(" "))
        assert(time.toInt > 0)
        assert(millisStr === " milliseconds")
      }
    }
  }
}
