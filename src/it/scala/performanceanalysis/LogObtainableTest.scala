package performanceanalysis

import akka.http.scaladsl.model.StatusCodes._
import org.scalatest.concurrent.ScalaFutures
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.utils.TwitterFutures

import scala.concurrent.duration._
import scala.language.postfixOps

class LogObtainableTest extends IntegrationTestBase with ScalaFutures with TwitterFutures {

  feature("Log Receiver should only support POST operations") {

    scenario("Logs posted at the LogReceiver") {
      Given("the server is running")
      val logLine = "some action took 101 seconds"
      val componentId: String = "logsObtainableComp"
      val response = asyncRegisterComponent(componentId)
      whenReady(response, timeout(1 second)) { result =>
        result.getStatusCode() shouldBe Created.intValue
        And(s"""I registered component "$componentId"""")
      }
      val logPath = s"/components/$componentId/logs"
      val logData = s"""{"logline" : "$logLine"}"""
      val logResponse = logReceiverPostResponse(logPath, logData)
      whenReady(logResponse, timeout(1 second)) { result =>
        result.getStatusCode() shouldBe Accepted.intValue
        And(s"""I posted a logline "$logLine" on this component""")
      }
      val getLogPath = s"/components/$componentId/logs"
      When(s"I do a GET to $getLogPath")
      val logGetResponse = adminGetResponse(getLogPath)
      whenReady(logGetResponse, timeout(1 second)) { result =>
        Then("the result should have statuscode 200")
        result.getStatusCode() shouldBe OK.intValue
        And(s"""the content should contain "$logLine"""")
        result.getContentString() should contain (logLine)
      }
    }
  }
}
