package performanceanalysis.logreceiver

import akka.http.scaladsl.testkit.ScalatestRouteTest
import performanceanalysis.base.SpecBase

/**
  * Created by Jordi on 5-4-2016.
  */
class LogReceiverSpec extends SpecBase with ScalatestRouteTest {
  "The log receiver" must {
    "handle a GET on /components is successfull" in new LogReceiver {
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
