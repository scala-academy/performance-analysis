package performanceanalysis

import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.model.StatusCodes.Accepted
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import performanceanalysis.base.SpecBase

import scala.language.postfixOps
import scala.concurrent.duration._


/**
  * Created by seeta on 5/31/16.
  */
class LogReceiverSimulation extends Simulation with SpecBase {
  private val administratorBaseURL = "http://localhost:9000"
  private val repeats = 2

  val compId = "log-comp-id13"

  object Administrator {
    val metricKey = "aMetricKey"
    val regex = "(\\\\d+ ms)"
    val componentsUrl = s"$administratorBaseURL/components"
    val registerMetricUrl = s"$administratorBaseURL/components/$compId/metrics"
    val alertingRuleUrl = s"$administratorBaseURL/components/$compId/metrics/$metricKey/alerting-rules"
    val admin = exec(
      http("register component")
        .post(componentsUrl)
        .body(StringBody(s"""{"componentId" : "$compId"}""")).asJSON
        .check(status.is(Created.intValue))
    ).pause(200 millis).exec(
      http("register metric")
        .post(registerMetricUrl)
        .body(StringBody(s"""{"regex" : "$regex", "metric-key" : "$metricKey", "value-type": "duration"}""")).asJSON
        .check(status.is(Created.intValue))
    ).pause(200 millis).exec(
      http("add alerting rule")
        .post(alertingRuleUrl)
        .body(StringBody("""{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}""")).asJSON
        .check(status.is(Created.intValue))
    ).pause(200 millis)
  }

  val adminConf = http
    .acceptHeader("application/json")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicSimulation").exec(Administrator.admin)
  val feeder = csv("logs.csv").random
  val logScenario = scenario("LogReceiverSimulation").repeat(repeats, "n") {
    val url = "http://localhost:9090/components/log-comp-id13/logs"
    exec(
      http("posting logs")
      .post(url)
        .body(StringBody("""{"logLines" : "${logLine1}\n${logLine2}"}""")).asJSON
        .check(status.is(Accepted.intValue))
    ).feed(feeder)
  }

  setUp(
    scn.inject(atOnceUsers(1)),
    logScenario.inject(rampUsers(8000) over (2 seconds))
  ).protocols(adminConf)
}
