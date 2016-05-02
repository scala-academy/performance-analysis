package performanceanalysis

import com.twitter.finagle.http
import org.scalatest.concurrent.ScalaFutures
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.utils.TwitterFutures

import scala.concurrent.duration._
import scala.language.postfixOps

class LogSubmissionTest extends IntegrationTestBase with ScalaFutures with TwitterFutures {

  feature("Log Receiver should only support POST operations") {
    scenario("No GET requests allowed") {
      Given("the server is running")
      When("I do a HTTP GET to '/' on the LogReceiver port")
      val request  = http.Request(http.Method.Get, "/")
      val response = performLogReceiverRequest(request)
      whenReady(response, timeout(1 second)) { result =>
        assert(result.getStatusCode() === 405)
        Then("the response should have status code 405")
      }
    }
    scenario("Logs posted at the LogReceiver") {
      Given("the server is running")

      val componentId: String = "parsingConfiguredComponent"
      And(s"I registered a component with id $componentId")
      registerComponent(componentId)

      val path = "/components/" + componentId
      val data = """{"regex" : "+d", "metric-key" : "a-numerical-metric"}"""

      And(s"also registered a metric $data to /components/$componentId on the Administrator port")
      awaitAdminPostResonse(path, data)

      val logPath = "/components/" + componentId + "/logs"
      val logData = """{"logline" : "some action took 101 seconds"}"""
      When(s"I do a POST with $logData to /components/$componentId/logs on the LogReceiver port")
      val response = logReceiverPostResponse(logPath, logData)

      whenReady(response, timeout(1 second)) { result =>
        assert(result.getStatusCode() === 202)
        Then("the response should have status code 202")
      }
    }
  }
}
