package performanceanalysis


import performanceanalysis.base.IntegrationTestBase
import spray.json._

/**
  * Created by steven on 20-4-16.
  */
class AlertRuleAdministrationTest extends IntegrationTestBase {

  feature("Alert rules") {
    scenario("Obtaining alert rule details") {
      Given("the server is running")

      val component = "logObtainableComp4"
      val metricKey = "a-numerical-metric"
      And(s"I registered component $component with a metric with metric-key $metricKey")
      val registerCompResponse = awaitAdminPostResponse("/components", s"""{"componentId" : "$component"}""")
      registerCompResponse.statusCode shouldBe 201

      val metricPayload = """{"regex" : "\\d+\\sms", "metric-key" : "a-numerical-metric"}"""
      val registerMetricResponse = awaitAdminPostResponse(s"/components/$component/metrics", metricPayload)
      registerMetricResponse.statusCode shouldBe 201

      val alertPayload = """{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}"""
      val path = s"/components/$component/metrics/a-numerical-metric/alerting-rules"
      And(s"an alerting rule has been registered to $path with payload $alertPayload")
      val registerAlertResponse = awaitAdminPostResponse(path, alertPayload)
      registerAlertResponse.statusCode shouldBe 201

      When(s"I do a GET to $path")
      val getAlertRuleResponse = awaitAdminGetResponse(path)

      Then("the result should have statuscode 200")
      getAlertRuleResponse.statusCode shouldBe 200

      And(s"the content should contain $alertPayload")
      getAlertRuleResponse.contentString.parseJson shouldBe alertPayload.parseJson
    }

    scenario("Alert rule deletion") {
      Given("the server is running")

      val component = "logObtainableComp2"
      val metricKey = "a-numerical-metric"
      And(s"I registered component $component with a metric with metric-key $metricKey")
      val registerCompResponse = awaitAdminPostResponse("/components", s"""{"componentId" : "$component"}""")
      registerCompResponse.statusCode shouldBe 201

      val metricPayload = """{"regex" : "\\d+\\sms", "metric-key" : "a-numerical-metric"}"""
      val registerMetricResponse = awaitAdminPostResponse(s"/components/$component/metrics", metricPayload)
      registerMetricResponse.statusCode shouldBe 201

      val alertPayload = """{"threshold": {"max": "2000 ms"}, "action": {"url": "dummy-action"}}"""
      val path = s"/components/$component/metrics/a-numerical-metric/alerting-rules"
      And(s"an alerting rule has been registered to $path with payload $alertPayload")
      val registerAlertResponse = awaitAdminPostResponse(path, alertPayload)
      registerAlertResponse.statusCode shouldBe 201

      When(s"I do a DELETE to $path")
      val deleteAlertRuleResponse = awaitAdminDeleteResponse(path)

      Then("the result should have statuscode 204")
      deleteAlertRuleResponse.statusCode shouldBe 204

      And(s"a GET to $path should return statuscode 404")
      val getAlertRuleResponse = awaitAdminGetResponse(path)
      getAlertRuleResponse.statusCode shouldBe 404
    }
  }
}
