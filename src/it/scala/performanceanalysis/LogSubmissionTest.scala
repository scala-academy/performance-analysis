package performanceanalysis

import performanceanalysis.base.IntegrationTestBase
import com.twitter.finagle.http
import com.twitter.finagle.http.Response
import org.scalatest.concurrent.ScalaFutures
import performanceanalysis.utils.TwitterFutures

import scala.concurrent.duration._

class LogSubmissionTest extends IntegrationTestBase with ScalaFutures with TwitterFutures {

  feature("Log Receiver should only support POST operations") {
    scenario("No GET requests allowed") {
      Given("the server is running")
      When("I do a HTTP GET to '/' on the LogReceiver port")
      val request  = http.Request(http.Method.Get, "/")
      val response = performLogReceiverRequest(request)
      whenReady(response, timeout(1.seconds)) { result =>
        assert(result.getStatusCode() === 405)
        Then("the response should have statuscode 405")
      }
    }
    scenario("Logs posted at the LogReceiver") {
      Given("the server is running")

      And("""I registered a component with id "parsingConfiguredComponent"""")
      registerComponent("parsingConfiguredComponent")

      And("""And a metric {"regex" : "+d", "metric-key" : "a-numerical-metric"} to /components/parsingConfiguredComponent on the Administrator port""")
      val path = "/components/parsingConfiguredComponent"
      val data = """{"regex" : "+d", "metric-key" : "a-numerical-metric"}"""
      awaitAdminPostResonse(path, data)

      When("""I do a POST with {"logline" : "some action took 101 seconds", "metric-key" : "a-numerical-metric"} to /components/parsingConfiguredComponent/logs on the """ +
          "LogReceiver port")
      val logPath = "/components/parsingConfiguredComponent"
      val logData = """{"logline" : "some action took 101 seconds", "metric-key" : "a-numerical-metric"}"""
      val response = logReceiverPostResponse(path, data)

      whenReady(response, timeout(1.seconds)) { result =>
        assert(result.getStatusCode() === 202)
        Then("""the response should have statuscode 202""")
      }
    }
  }

  def registerComponent(componentId: String): Response = {
    val path = "/components"
    val data = s"""{"componentId" : "${componentId}"}"""
    awaitAdminPostResonse(path, data)
  }

}
