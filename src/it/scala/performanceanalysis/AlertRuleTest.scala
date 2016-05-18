package performanceanalysis

import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.server.Protocol


class AlertRuleTest extends IntegrationTestBase with Protocol {

  feature("Altering rules") {
    scenario("Register an alerting rule") {
      Given("the server is running")

      And("""I registered component "logsObtainableComp" with a metric with metric-key "a-numerical-metric" """)
      val registerCompRequest = buildPostRequest(adminRequestHost, "/components", """{"componentId" : "logsObtainableComp"}""")
      val registerCompResponseFuture = performAdminRequest(registerCompRequest)
      val registerCompResponse = Await.result(registerCompResponseFuture)
      registerCompResponse.statusCode shouldBe 201

      And("""I register a metric with metric-key "a-numerical-metric" """)
      val registerMetricRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp/metrics",
        """{"regex" : "\\d+\\sms", "metric-key" : "a-numerical-metric"}""")
      val registerMetricResponseFuture = performAdminRequest(registerMetricRequest)
      val registerMetricResponse = Await.result(registerMetricResponseFuture)
      registerMetricResponse.statusCode shouldBe 201

      val alertPayload = "{\"threshold\": {\"max\": \"2000 ms\"}, \"action\": {\"url\": \"dummy-action\"}}"

      When("""I do a POST to /components/logsObtainableComp/metrics/a-numerical-metric/alerting-rules" """)
      val registerAlertRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp/metrics/a-numerical-metric/alerting-rules",
        alertPayload)
      val registerAlertResponseFuture = performAdminRequest(registerAlertRequest)
      val registerAlertResponse = Await.result(registerAlertResponseFuture)
      registerAlertResponse.statusCode shouldBe 201

      When("""When I do a POST to /components/logsObtainableComp/metrics/unknown-metric-key/alerting-rules" """)
      val registerAlertToFailRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp/metrics/unknown-metric-key/alerting-rules",
        alertPayload)
      val registerAlertToFailResponseFuture = performAdminRequest(registerAlertToFailRequest)
      val registerAlertToFailResponse = Await.result(registerAlertToFailResponseFuture)
      registerAlertToFailResponse.statusCode shouldBe 404
    }
  }
}
