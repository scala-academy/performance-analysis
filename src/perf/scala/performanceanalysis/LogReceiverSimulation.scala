package performanceanalysis

import akka.http.scaladsl.model.StatusCodes.Accepted
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import performanceanalysis.base.SpecBase

import scala.concurrent.duration._
import scala.language.postfixOps
import scalaj.http.Http



/**
  * Created by seeta on 5/31/16.
  */
class LogReceiverSimulation extends Simulation with SpecBase {
  private val administratorBaseURL = "http://localhost:9000"
  private val repeats = 1

  private val compId = "log-comp-id1225"

  private def post(url: String, data: String) = {
    Http(url).postData(data).header("content-type", "application/json").asString
  }

  before {
    val metricKey = "aMetricKey"
    val regex = "(\\\\d+ ms)"
    val componentsUrl = s"$administratorBaseURL/components"
    val registerMetricUrl = s"$administratorBaseURL/components/$compId/metrics"
    val alertingRuleUrl = s"$administratorBaseURL/components/$compId/metrics/$metricKey/alerting-rules"

    post(componentsUrl, s"""{"componentId" : "$compId"}""")
    post(registerMetricUrl, s"""{"regex" : "$regex", "metric-key" : "$metricKey", "value-type": "duration"}""")
    post(alertingRuleUrl, """{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}""")
  }

  val httpConf = http
    .acceptHeader("application/json")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val feeder = csv("logs.csv").random
  val logScenario = scenario("LogReceiverSimulation").repeat(repeats, "counter") {
    val url = s"http://localhost:9090/components/$compId/logs"
    exec(
      http("posting logs")
      .post(url)
        .body(StringBody("""{"logLines" : "some action took 200 ms\nsome action took 201 ms"}""")).asJSON
        .check(status.is(Accepted.intValue))
    ).feed(feeder)
  }

  setUp(logScenario.inject(rampUsers(1000) over (5 seconds))).protocols(httpConf)
}
