package performanceanalysis.logreceiver

import akka.http.scaladsl.testkit.ScalatestRouteTest
import performanceanalysis.base.SpecBase

/**
  * Created by Ruud on 12-04-16.
  */
class LogSubmissionTest extends SpecBase with ScalatestRouteTest {
  "The log receiver" must {
    "handle a GET on /components is successful" in new LogReceiver {
      Get("/components") ~> routes ~> check {
        status.isSuccess() shouldEqual true
      }
    }
    "handle a GET on /components response with status code 200" in new LogReceiver {
      Get("/components") ~> routes ~> check {
        status.intValue() shouldEqual 200
      }
    }
    "handle a GET on /components response with status code 405" in new LogReceiver {
      Get("/") ~> routes ~> check {
        status.intValue() shouldEqual 405
      }
    }
  }
}
