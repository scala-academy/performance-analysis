package performanceanalysis


import com.twitter.finagle.http.Response
import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.server.Protocol
import spray.json._

/**
  * Created by steven on 20-4-16.
  */
class AlertRuleAdministrationTest extends IntegrationTestBase with Protocol {

  override def awaitAdminPostResponse(path: String, data: String): Response = {
    val request = buildPostRequest(adminRequestHost, path, data)
    Await.result(performAdminRequest(request))
  }

  feature("Alert rules") {
    scenario("Obtaining alert rule details") {
      Given("le server is running")

      And("""I registered component "comp" with a metric with metric-key "a-numerical-metric" """)
      val registerCompResponse = awaitAdminPostResponse("/components", """{"componentId" : "comp"}""")
      registerCompResponse.statusCode shouldBe 201

      And("""I register a metric with metric-key "a-metric" """)
      val registerMetricResponse = awaitAdminPostResponse("/components/comp/metrics",
        """{"regex" : "\\d+\\sms", "metric-key" : "a-metric", "value-type": "duration"}""")
      registerMetricResponse.statusCode shouldBe 201

      val alertPayload = """{"when": "_ < 2000 ms", "action": {"url": "dummy-action"}}"""
      And("I registered an AlertRule")
      val registerAlertResponse = awaitAdminPostResponse("/components/comp/metrics/a-metric/alerting-rules",
        alertPayload)
      registerAlertResponse.statusCode shouldBe 201

      When("I do a GET on the path")
      val getAlertsResponse = awaitAdminGetResponse("/components/comp/metrics/a-metric/alerting-rules")
      getAlertsResponse.statusCode shouldBe 200
      getAlertsResponse.contentString.parseJson shouldBe s"""{"alertRules": [$alertPayload]}""".parseJson
    }

    scenario("Alert rule deletion") {
      Given("the server is running")

      And("""I registered component "comp2" with a metric with metric-key "a-numerical-metric" """)
      val registerCompResponse = awaitAdminPostResponse("/components", """{"componentId" : "comp2"}""")
      registerCompResponse.statusCode shouldBe 201

      And("""I register a metric with metric-key "a-metric" """)
      val registerMetricResponse = awaitAdminPostResponse("/components/comp2/metrics",
        """{"regex" : "\\d+\\sms", "metric-key" : "a-metric", "value-type": "duration"}""")
      registerMetricResponse.statusCode shouldBe 201

      val alertPayload = """{"when": "_ < 2000 ms", "action": {"url": "dummy-action"}}"""

      And("I registered an AlertRule")
      val registerAlertResponse = awaitAdminPostResponse("/components/comp2/metrics/a-metric/alerting-rules", alertPayload)
      registerAlertResponse.statusCode shouldBe 201

      When(s"I do a DELETE to /components/comp2/metrics/a-metric/alerting-rules")
      val deleteRequest = buildDeleteRequest(adminRequestHost, "/components/comp2/metrics/a-metric/alerting-rules")
      val deleteResponseFuture = performAdminRequest(deleteRequest)
      val deleteAlertRuleResponse = Await.result(deleteResponseFuture)

      Then("the result should have statuscode 204")
      deleteAlertRuleResponse.statusCode shouldBe 204

      And(s"a GET to /components/comp2/metrics/a-metric/alerting-rules should return {alertRules: []}")
      val getAlertsRequest = buildGetRequest(adminRequestHost, "/components/comp2/metrics/a-metric/alerting-rules")
      val getAlertsResponseFuture = performAdminRequest(getAlertsRequest)
      val getAlertsResponse = Await.result(getAlertsResponseFuture)
      getAlertsResponse.statusCode shouldBe 200
      getAlertsResponse.contentString.parseJson shouldBe """{"alertRules": []}""".parseJson
    }
  }
}
