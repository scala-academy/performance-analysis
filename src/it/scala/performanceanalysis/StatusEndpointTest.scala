package performanceanalysis

import com.twitter.finagle.http.{Response, Method, Request}
import com.twitter.util.Await
import performanceanalysis.base.IntegrationTestBase

class StatusEndpointTest extends IntegrationTestBase {

  feature("Server status endpoint") {
    scenario("returns server uptime") {
      Given("the server is running")

      val path = "/status"
      val responseFuture = performAdminRequest(Request(Method.Get, path))
      When(s"I do a HTTP GET to '$path'")

      val response: Response = Await.result(responseFuture)
      response.statusCode shouldBe 200
      Then("the response should have statuscode 200")
    }
  }
  feature("Server components endpoint") {

    scenario("no registered components") {
      Given("the server is running")

      val path = "/components"
      When(s"I do a HTTP GET to '$path'")

      Then("the response should have statuscode 200")
      And("the content should be an empty list")
      // TODO: Implement
    }
    scenario("some registered components") {
      Given("the server is running")

      val path = "/components"
      // TODO: Implement
      And(s"I registered a component by doing a POST to $path")

      // TODO: Implement
      When(s"I do a HTTP GET to '$path'")

      // TODO: Implement
      Then("the response should have statuscode 200")
      And("the content should be an empty list")
    }
  }
}
