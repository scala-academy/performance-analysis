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

    scenario("Component registration with dateformat") {
      Given("the server is running")

      val path = "/components"
      val data = """{"componentId" : "TestComponentYMD", "dateFormat": "ymd"}"""
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
      getComponentsResponse.contentString.parseJson shouldBe """{"componentIds": ["TestComponent", "TestComponentYMD"]}""".parseJson

    }

    scenario("Configure parsing of a log line with a single metric") {

      Given("the server is running")

      And("""I registered a component with id "parsConfigComp"""")
      awaitRegisterComponent("parsConfigComp")

      val path = "/components/parsConfigComp/metrics"
      val data = """{"regex" : "+d", "metric-key" : "a-numerical-metric", "value-type": "string"}"""
      When(s"""I do a POST with $data to $path on the Administrator port""")

      val parseResponse = awaitAdminPostResponse(path, data)

      Then("""the response should have statuscode 201""")
      parseResponse.statusCode shouldBe 201
    }

    scenario("Obtain detail of a component") {

      Given("the server is running")

      And("""I registered a component with id "parsConfigComp"""")
      awaitRegisterComponent("parsConfigComp2")

      val path = "/components/parsConfigComp2/metrics"
      val data = """{"regex" : "+d", "metric-key" : "a-numerical-metric", "value-type": "string"}"""
      When(s"""And I did a POST with $data to $path on the Administrator port""")
      awaitAdminPostResponse(path, data)

      When("I do a GET to /components/parsConfigComp2/metrics")
      val response = awaitAdminGetResponse("/components/parsConfigComp2/metrics")

      Then("""the response should have statuscode 200""")
      response.statusCode shouldBe 200

      And(s"""And the content should contain $data""")
      val result = response.contentString.parseJson.convertTo[Map[String, List[Map[String, String]]]]
      assert(result("metrics").contains(Map("regex" -> "+d", "metric-key" -> "a-numerical-metric", "value-type" -> "string")))
    }
  }
}
