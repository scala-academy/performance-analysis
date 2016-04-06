package performanceanalysis

import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase

class ComponentRegistrationTest extends IntegrationTestBase {

  feature("Server components endpoint") {
    scenario("Component registration at the Administrator") {
      Given("the server is running")

      val path = "/components"
      val data = """{"componentId" : "TestComponent"}"""
      val registerRequest = buildPostRequest(adminRequestHost, path, data)
      val registerResponseFuture = performAdminRequest(registerRequest)
      val registerResponse = Await.result(registerResponseFuture)
      And(s"I registered a component by doing a POST with $data to $path")
      registerResponse.statusCode shouldBe 501 // TODO Adjust to 200

      // TODO: Implement
      When(s"I do a HTTP GET to $path on the Administrator port")

      // TODO: Implement
      Then("the response should have statuscode 200")

      // TODO: Implement
      And(s"the content should be a list containing $data")
    }
    scenario("Configure parsing of a log line with a single metric") {
      Given("the server is running")
      And("""I registered a component with id "parsConfigComp"""")
      When("""I do a POST with {"regex" : "+d", "metric-key" : "a-numerical-metric"} to /components/parsConfigComp on the Administrator port""")
      Then("""the response should have statuscode 201""")
    }
  }
}
