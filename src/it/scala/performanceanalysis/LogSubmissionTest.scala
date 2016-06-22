package performanceanalysis

import akka.http.scaladsl.model.StatusCodes.{Accepted, Created, MethodNotAllowed}
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
        result.statusCode shouldBe MethodNotAllowed.intValue
        Then("the response should have status code 405")
      }
    }

    scenario("Logs posted at the LogReceiver") {
      Given("the server is running")

      val componentId: String = "parsingConfiguredComponent"
      And(s"registered a component with id $componentId")
      awaitRegisterComponent(componentId).getStatusCode() shouldBe Created.intValue

      val metricPath = s"/components/$componentId/metrics"
      val metricKey = "aKey"
      val regex: String = """(\\d+ ms)"""
      val data = s"""{"regex" : "$regex", "metric-key" : "$metricKey", "value-type": "duration"}"""

      And(s"also registered a metric $data to $metricPath on the Administrator port")
      awaitAdminPostResponse(metricPath, data).statusCode shouldBe Created.intValue

      val registerAlertRule = s"/components/$componentId/metrics/$metricKey/alerting-rules"
      val rule = """{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}"""

      And(s"also registered a alerting rule $rule to $registerAlertRule on the Administrator port")
      awaitAdminPostResponse(registerAlertRule, rule).statusCode shouldBe Created.intValue

      val logPath = s"/components/$componentId/logs"
      val logData = """{"logLines" : "some action took 200 ms\nsome action took 201 ms"}"""
      When(s"$logData POST to $logPath on the LogReceiver port")
      val response = logReceiverPostResponse(logPath, logData)

      whenReady(response, timeout(1 second)) { result =>
        result.statusCode shouldBe Accepted.intValue
        Then("the response should have status code 202")
      }

      val logDateData = """{"logLines" : "[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms"}"""
      When(s"I POST $logDateData to $logPath")
      val response2 = awaitLogReceiverPostResonse(logPath, logDateData)
      response2.statusCode shouldBe Accepted.intValue
    }
  }
}
