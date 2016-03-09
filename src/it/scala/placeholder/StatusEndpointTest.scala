package placeholder

import com.twitter.finagle.http.{Response, Method, Request}
import com.twitter.util.Await
import placeholder.base.IntegrationTestBase

class StatusEndpointTest extends IntegrationTestBase {

  feature("Server status endpoint") {
    scenario("returns server uptime") {
      Given("the server is running")

      val path = "/status"
      val responseFuture = performRequest(Request(Method.Get, path))
      When(s"I do a HTTP GET to '$path'")

      val response: Response = Await.result(responseFuture)
      response.statusCode shouldBe 200
      Then("the response should have statuscode 200")
    }
  }
}
