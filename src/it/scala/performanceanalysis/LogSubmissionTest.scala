package performanceanalysis

import performanceanalysis.base.IntegrationTestBase

class LogSubmissionTest extends IntegrationTestBase {

  feature("Log Receiver should only support POST operations") {
    scenario("") {
      Given("the server is running")
      When("I do a HTTP GET to '/' on the LogReceiver port")
      Then("the response should have statuscode 405")
    }
    scenario("Logs posted at the LogReceiver") {
      // TODO Implement
      Given("the server is running")
      And("""I registered a component with id "parsingConfiguredComponenet" and a metric {"regex" : "+d", "metric-key" : "a-numerical-metric"}""")
      When("""I do a POST with {"logline" : "some action took 101 seconds", "metric-key" : "a-numerical-metric"} to /components/parsConfigComp/logs on the """ +
          "LogReceiver port")
      Then("""the response should have statuscode 202""")
    }
  }
}
