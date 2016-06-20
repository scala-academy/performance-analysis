package performanceanalysis

import akka.http.scaladsl.model.StatusCodes.Created
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import performanceanalysis.base.SpecBase

import scala.language.postfixOps


/**
  * Created by seeta on 5/31/16.
  */
class AdministratorSimulation extends Simulation with SpecBase {
  private val numberOfRepeats = 100
  private val baseURL = "http://localhost:9000"

  val httpConf = http
    .baseURL(baseURL)
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("BasicSimulation").repeat(numberOfRepeats, "n") {
    val compId = "compId${n}"
    val metricKey = "metrickey"
    val regex = "(\\\\d+ ms)"
    val registerMetricUrl = s"/components/$compId/metrics"
    val alertingRuleUrl = s"/components/$compId/metrics/$metricKey/alerting-rules"
    exec(
      http("register component")
        .post("/components")
        .body(StringBody(s"""{"componentId" : "$compId"}""")).asJSON
        .check(status.is(Created.intValue))
    ).exec(
      http("register metric")
        .post(registerMetricUrl)
        .body(StringBody(s"""{"regex" : "$regex", "metric-key" : "$metricKey"}""")).asJSON
        .check(status.is(Created.intValue))
    ).exec(
      http("add alerting rule")
        .post(alertingRuleUrl)
        .body(StringBody("""{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}""")).asJSON
        .check(status.is(Created.intValue))
    )
  }

  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf)
}
