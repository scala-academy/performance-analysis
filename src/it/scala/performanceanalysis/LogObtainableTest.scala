package performanceanalysis

import akka.http.scaladsl.model.StatusCodes._
import org.scalatest.concurrent.ScalaFutures
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.utils.TwitterFutures

import scala.concurrent.duration._
import scala.language.postfixOps

class LogObtainableTest extends IntegrationTestBase with ScalaFutures with TwitterFutures {

  feature("Parsed metrics obtainable through Administrator") {

    scenario("Logs posted only at the LogReceiver") {
      Given("the server is running")
      val logLine = "some action took 101 seconds"
      val componentId: String = "logsObtainableCompA"
      val response = registerComponent(componentId)
      whenReady(response, timeout(1 second)) { result =>
        And(s"""I registered component "$componentId"""")
        result.getStatusCode() shouldBe Created.intValue
      }
      val logPath = s"/components/$componentId/logs"
      val logResponse = logReceiverPostResponse(logPath, logLine)
      whenReady(logResponse, timeout(1 second)) { result =>
        And(s"""I posted a logline "$logLine" on this component""")
        result.getStatusCode() shouldBe Accepted.intValue
      }
      val getLogPath = s"/components/$componentId/logs"
      When(s"I do a GET to $getLogPath")
      val logGetResponse = adminGetResponse(getLogPath)
      whenReady(logGetResponse, timeout(1 second)) { result =>
        Then("the result should have statuscode 200")
        result.getStatusCode() shouldBe OK.intValue
        And(s"""the content should contain "$logLine"""")
        result.getContentString() shouldBe (logLine)
      }
    }
    scenario("Logs posted and metrics registered") {
      Given("the server is running")
      val logLine = "some action took 101 seconds"
      val componentId: String = "logsObtainableCompB"
      val response = registerComponent(componentId)
      whenReady(response, timeout(1 second)) { result =>
        And(s"""I registered component "$componentId"""")
        result.getStatusCode() shouldBe Created.intValue
      }
      val metricsPath = s"/components/$componentId/metrics"
      val metricsData = """{"regex" : "\\d+", "metric-key" : "a-numerical-metric", "value-type": "duration"}"""
      val metricsResponse = adminPostResponse(metricsPath, metricsData)
      whenReady(metricsResponse, timeout(1 second)) { result =>
        And("""with a metric with metric-key "a-numerical-metric"""")
        result.getStatusCode() shouldBe Created.intValue
      }
      val logPath = s"/components/$componentId/logs"
      val logResponse = logReceiverPostResponse(logPath, logLine)
      whenReady(logResponse, timeout(1 second)) { result =>
        result.getStatusCode() shouldBe Accepted.intValue
        And(s"""I posted a logline "$logLine" on this component""")
        And(s"""that logline is parsed by the metric "a-numerical-metric"""")
      }
      val getLogPath = s"/components/$componentId/metrics/a-numerical-metric"
      When(s"I do a GET to $getLogPath")
      val logGetResponse = adminGetResponse(getLogPath)
      whenReady(logGetResponse, timeout(1 second)) { result =>
        Then("the result should have statuscode 200")
        result.getStatusCode() shouldBe OK.intValue
        And("""the content should contain "101"""")
        result.getContentString() shouldBe ("101")
      }
    }
  }
}
