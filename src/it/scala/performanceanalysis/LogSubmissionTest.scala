package performanceanalysis

import performanceanalysis.base.IntegrationTestBase

class LogSubmissionTest extends IntegrationTestBase {

  feature("Log Receiver endpoint") {
    scenario("Logs posted at the LogReceiver") {
      // TODO Implement
      Given("the server is running")
      And("""I did not register a component with name "notRegistered"""")
      When("I do a HTTP POST to '/components/notRegistered/metrics' on the LogReceiver port")
      Then("the response should have statuscode 404")

      Given("the server is running")
      And("""I registered a component by doing a POST with {"componentId" : "LogReceivingComponent"} to /components on the Administrator port""")
      When("""I do a HTTP POST with {"metric-key" : "123"} to '/components/LogReceivingComponent/metrics' on the LogReceiver port""")
      Then("the response should have statuscode 202")
    }
  }
}
