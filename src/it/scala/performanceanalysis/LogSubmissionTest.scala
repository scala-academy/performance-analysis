package performanceanalysis

import performanceanalysis.base.IntegrationTestBase
import com.twitter.finagle.http
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
      // TODO Implement
      Given("the server is running")
      And("""I registered a component with id "parsingConfiguredComponenet" and a metric {"regex" : "+d", "metric-key" : "a-numerical-metric"}""")
      When("""I do a POST with {"logline" : "some action took 101 seconds", "metric-key" : "a-numerical-metric"} to /components/parsConfigComp/logs on the """ +
          "LogReceiver port")
      Then("""the response should have statuscode 202""")
    }
  }
}
