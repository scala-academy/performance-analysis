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
    scenario("Components registered at the Administrator should be known at the LogReceiver") {
      Given("the server is running")

      val path = "/components"
      val data = """{"componentId" : "TestComponent"}"""
      val registerRequest = buildPostRequest(adminRequestHost, path, data)
      val registerResponseFuture = performAdminRequest(registerRequest)
      val registerResponse = Await.result(registerResponseFuture)
      And(s"I registered a component by doing a POST with $data to $path")
      registerResponse.statusCode shouldBe 501 // TODO Adjust to 200

      // TODO: Implement
      When(s"I do a HTTP GET to $path on the LogReceiver port")

      // TODO: Implement
      Then("the response should have statuscode 200")

      // TODO: Implement
      And(s"the content should be a list containing $data")
    }
  }
}
