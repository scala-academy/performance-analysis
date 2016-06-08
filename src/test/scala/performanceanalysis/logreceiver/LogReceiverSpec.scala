package performanceanalysis.logreceiver

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.{Accepted, MethodNotAllowed, NotFound, OK}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import performanceanalysis.base.SpecBase
import performanceanalysis.server.messages.AdministratorMessages._
import performanceanalysis.server.messages.LogMessages._

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
        status shouldBe MethodNotAllowed
      }
    }

    "handle a GET on /components response with status code 200" in new LogReceiver {
      Get("/components") ~> Route.seal(routes) ~> check {
        status shouldBe OK
      }
    }

    "handle a POST on /components/<existing-compId>/logs respond with status code 202" in new LogReceiverProbe() {
      val results = Post(s"/components/$componentId/logs", logLine) ~> routes

      probe.expectMsg(SubmitLog(componentId, logLine))
      probe.reply(LogSubmitted)

      results ~> check {
        response.status shouldBe Accepted
      }
    }

    "handle a POST on /components/<non-existing-compId>/logs respond with status code 404" in new LogReceiverProbe() {
      val testResult = Post(s"/components/$componentId/logs", logLine) ~> routes

      probe.expectMsg(SubmitLog(componentId, logLine))
      probe.reply(LogParserNotFound(componentId))

      testResult ~> check {
        response.status shouldBe NotFound
      }
    }
  }
}