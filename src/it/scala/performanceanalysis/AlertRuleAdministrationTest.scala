package performanceanalysis

import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase
import spray.json._

/**
  * Created by steven on 20-4-16.
  */
class AlertRuleAdministrationTest extends IntegrationTestBase {

  feature("Alert rules") {
    scenario("Obtaining alert rule details") {
      Given("the server is running")

      val component = "logObtainableComp"
      And("I registered component $component with a metric with metric-key a-numerical-metric")
      val registerCompRequest = buildPostRequest(adminRequestHost, "/components", """{"componentId" : "logsObtainableComp"}""")
      val registerCompResponseFuture = performAdminRequest(registerCompRequest)
      val registerCompResponse = Await.result(registerCompResponseFuture)
      registerCompResponse.statusCode shouldBe 201

      val registerMetricRequest = buildPostRequest(adminRequestHost, "/components/logsObtainableComp/metrics",
        """{"regex" : "\\d+\\sms", "metric-key" : "a-numerical-metric"}""")
      val registerMetricResponseFuture = performAdminRequest(registerMetricRequest)
      val registerMetricResponse = Await.result(registerMetricResponseFuture)
      registerMetricResponse.statusCode shouldBe 201

      val payload = """{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}""".parseJson
      val path = s"/components/$component/metrics/a-numerical-metric/alerting-rules"
      And(s"an alerting rule has been registered to $path with payload $payload")

      When(s"I do a GET to $path")

      Then("the result should have statuscode 200")

      And(s"the content should contain $payload")
    }
    scenario("Alert rule deletion") {
      Given("the server is running")

      val component = "logObtainableComp"
      And("I registered component $component with a metric with metric-key a-numerical-metric")

      val payload = """{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}"""
      val path = s"/components/$component/metrics/a-numerical-metric/alerting-rules"
      And(s"an alerting rule has been registered to $path with payload $payload")

      When(s"I do a DELETE to $path")
      Then("the result should have statuscode 204")
      And(s"a GET to $path should return statuscode 404")

    }
  }
}
