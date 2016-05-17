package performanceanalysis

import akka.http.scaladsl.model.StatusCodes.{Created, OK}
import org.scalatest.concurrent.ScalaFutures
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.utils.TwitterFutures

import scala.concurrent.duration._
import scala.language.postfixOps

class LogObtainableTest extends IntegrationTestBase with ScalaFutures with TwitterFutures {

  feature("Parsed metrics obtainable through Administrator") {

    scenario("Metrics obtainable through Administrator") {
      Given("the server is running")

      val logLine = "some action took 101 seconds"
      val componentId: String = "logsObtainableComp"
      And(s"registered a component with id $componentId")
      registerComponent(componentId).getStatusCode() shouldBe Created.intValue

      val logPath = s"/components/$componentId/logs"
      val logData = s"""{"log" : "$logLine"}"""

      And(s"""And I posted a logline "$logLine" on this component on the Administrator port""")
      awaitAdminPostResonse(logPath, logData).getStatusCode() shouldBe Created.intValue

      val getLogPath = s"/components/$componentId/logs"
      When(s"I do a GET to $getLogPath")
      val response = adminGetResponse(getLogPath)

      whenReady(response, timeout(1 second)) { result =>
        Then("the result should have statuscode 200")
        result.getStatusCode() shouldBe OK
        And(s"""And the content should contain "$logLine"""")
        result.getContentString() shouldBe logLine
      }
    }
  }
}
