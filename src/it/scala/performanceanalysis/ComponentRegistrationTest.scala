package performanceanalysis

import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase
import performanceanalysis.server.Protocol
import spray.json._


class ComponentRegistrationTest extends IntegrationTestBase with Protocol {

  feature("Server components endpoint") {
    scenario("Component registration at the Administrator") {
      Given("the server is running")

      val path = "/components"
      val data = """{"componentId" : "TestComponent"}"""
      val registerRequest = buildPostRequest(adminRequestHost, path, data)
      val registerResponseFuture = performAdminRequest(registerRequest)
      val registerResponse = Await.result(registerResponseFuture)
      And(s"I registered a component by doing a POST with $data to $path")
      registerResponse.statusCode shouldBe 201

      When(s"I do a HTTP GET to $path on the Administrator port")
      val getComponentsRequest = buildGetRequest(adminRequestHost, path)
      val getComponentsResponse = Await.result(performAdminRequest(getComponentsRequest))

      Then("the response should have statuscode 200")
      getComponentsResponse.statusCode shouldBe 200

      And(s"the content should be a list containing $data")
      getComponentsResponse.contentString.parseJson shouldBe """{"componentIds": ["TestComponent"]}""".parseJson

    }

    scenario("Configure parsing of a log line with a single metric") {

      Given("the server is running")

      And("""I registered a component with id "parsConfigComp"""")
      registerComponent("parsConfigComp")

      When("""I do a POST with {"regex" : "+d", "metric-key" : "a-numerical-metric"} to /components/parsConfigComp on the Administrator port""")
      val path = "/components/parsConfigComp"
      val data = """{"regex" : "+d", "metric-key" : "a-numerical-metric"}"""
      val parseResponse = awaitAdminPostResonse(path, data)

      Then("""the response should have statuscode 201""")
      parseResponse.statusCode shouldBe 201
    }

    scenario("Obtain detail of a component") {

      Given("the server is running")

      And("""I registered a component with id "parsConfigComp"""")
      registerComponent("parsConfigComp2")

      When("""And I did a POST with {"regex" : "+d", "metric-key" : "a-numerical-metric"} to /components/parsConfigComp2 on the Administrator port""")
      val path = "/components/parsConfigComp2"
      val data = """{"regex" : "+d", "metric-key" : "a-numerical-metric"}"""
      awaitAdminPostResonse(path, data)

      When("I do a GET to /components/parsConfigComp2/metrics")
      val response = awaitAdminGetResonse("/components/parsConfigComp2/metrics")

      Then("""the response should have statuscode 200""")
      response.statusCode shouldBe 200

      And("""And the content should contain {"regex" : "+d", "metric-key" : "a-numerical-metric"}""")
      val result = response.contentString.parseJson.convertTo[Map[String, List[Map[String, String]]]]
      assert(result("metrics").contains(Map("regex" -> "+d", "metric-key" -> "a-numerical-metric")))
    }
  }
}
