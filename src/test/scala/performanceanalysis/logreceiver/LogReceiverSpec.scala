package performanceanalysis.logreceiver

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import performanceanalysis.base.SpecBase
import performanceanalysis.server.Protocol.{LogParserNotFound, LogSubmitted, SubmitLog}

/**
  * Created by Jordi on 5-4-2016.
  */
class LogReceiverSpec extends SpecBase with ScalatestRouteTest {
  class LogReceiverProbe extends LogReceiver {
    val logLine: String = "some log line"
    val probe = TestProbe("test-log-receiver")

    override val logReceiverActor: ActorRef = probe.ref
  }
  "The log receiver" must {
    val componentId = "someCompId"

    "handle a GET on / response with status code 405" in new LogReceiver {
      Get() ~> Route.seal(routes) ~> check {
        status === StatusCodes.MethodNotAllowed
      }
    }

    "handle a GET on /components response with status code 200" in new LogReceiver {
      Get("/components") ~> Route.seal(routes) ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    "handle a POST on /components/<existing-compId>/logs respond with status code 202" in new LogReceiverProbe() {
      val results = Post(s"/components/$componentId/logs", logLine) ~> routes

      probe.expectMsg(SubmitLog(componentId, logLine))
      probe.reply(LogSubmitted)

      results ~> check {
        response.status shouldBe StatusCodes.Accepted
      }
    }

    "handle a POST on /components/<non-existing-compId>/logs respond with status code 404" in new LogReceiverProbe() {
      val testResult = Post(s"/components/$componentId/logs", logLine) ~> routes

      probe.expectMsg(SubmitLog(componentId, logLine))
      probe.reply(LogParserNotFound(componentId))

      testResult ~> check {
        response.status shouldBe StatusCodes.NotFound
      }
    }
  }
}