package performanceanalysis


import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.server.Protocol

/**
  * Created by steven on 20-4-16.
  */
class AlertRuleAdministrationTest extends IntegrationTestBase with Protocol {

  feature("Alert rules") {
    scenario("Obtaining alert rule details") {
      Given("le server is running")

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

      And("I registered an AlertRule")
      val registerAlertRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp/metrics/a-numerical-metric/alerting-rules",
        alertPayload)
      val registerAlertResponseFuture = performAdminRequest(registerAlertRequest)
      val registerAlertResponse = Await.result(registerAlertResponseFuture)
      registerAlertResponse.statusCode shouldBe 201

      When("I do a GET on the path")
      val getAlertRuleResponse = awaitAdminGetResponse("/components/logsObtainableComp/metrics/a-numerical-metric/alerting-rules")
      getAlertRuleResponse.statusCode shouldBe 200

    }

    scenario("Alert rule deletion") {
      Given("the server is running")


      And("""I registered component "logsObtainableComp" with a metric with metric-key "a-numerical-metric" """)
      val registerCompRequest = buildPostRequest(adminRequestHost, "/components", """{"componentId" : "logsObtainableComp2"}""")
      val registerCompResponseFuture = performAdminRequest(registerCompRequest)
      val registerCompResponse = Await.result(registerCompResponseFuture)

      registerCompResponse.statusCode shouldBe 201

      And("""I register a metric with metric-key "a-numerical-metric" """)
      val registerMetricRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp2/metrics",
        """{"regex" : "\\d+\\sms", "metric-key" : "a-numerical-metric"}""")
      val registerMetricResponseFuture = performAdminRequest(registerMetricRequest)
      val registerMetricResponse = Await.result(registerMetricResponseFuture)
      registerMetricResponse.statusCode shouldBe 201

      val alertPayload = "{\"threshold\": {\"max\": \"2000 ms\"}, \"action\": {\"url\": \"dummy-action\"}}"

      And("I registered an AlertRule")
      val registerAlertRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp2/metrics/a-numerical-metric/alerting-rules",
        alertPayload)
      val registerAlertResponseFuture = performAdminRequest(registerAlertRequest)
      val registerAlertResponse = Await.result(registerAlertResponseFuture)
      registerAlertResponse.statusCode shouldBe 201

      When(s"I do a DELETE to /components/logsObtainableComp2/metrics/a-numerical-metric/alerting-rules")
      val deleteRequest = buildDeleteRequest(adminRequestHost, "/components/logsObtainableComp2/metrics/a-numerical-metric/alerting-rules")
      val deleteResponseFuture = performAdminRequest(deleteRequest)
      val deleteAlertRuleResponse = Await.result(deleteResponseFuture)

      Then("the result should have statuscode 204")
      deleteAlertRuleResponse.statusCode shouldBe 204

      And(s"a GET to /components/logsObtainableComp2/metrics/a-numerical-metric/alerting-rules should return statuscode 404")
      val getAlertsRequest = buildGetRequest(adminRequestHost, "/components/logsObtainableComp2/metrics/a-numerical-metric/alerting-rules")
      val getAlertsResponseFuture = performAdminRequest(getAlertsRequest)
      val getAlertsResponse = Await.result(getAlertsResponseFuture)
      getAlertsResponse.statusCode shouldBe 404

    }
  }
}
