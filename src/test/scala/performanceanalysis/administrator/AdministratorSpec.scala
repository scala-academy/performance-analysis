package performanceanalysis.administrator

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import performanceanalysis.base.SpecBase
import performanceanalysis.server.Protocol._

/**
  * Created by Jordi on 9-3-2016.
  */
class AdministratorSpec extends SpecBase with ScalatestRouteTest {

  class AdministratorWithProbe extends Administrator(system.deadLetters) {

    val probe = TestProbe()

    override protected val administratorActor = probe.ref

  }

  class TestAdministrator()

  "The server" must {
    "create an administrator actor and route messages to it" in new AdministratorWithProbe() {
      val routeTestResult = Get("/components") ~> routes

      probe.expectMsg(GetRegisteredComponents)
      probe.reply(RegisteredComponents(Set()))

      routeTestResult ~> check {
        status shouldBe StatusCodes.OK
        responseAs[RegisteredComponents] shouldBe RegisteredComponents(Set())
      }
    }

    "handle a GET on /components by returning all registered componentIds" in new AdministratorWithProbe() {
      val testRouteResult = Get("/components") ~> routes

      probe.expectMsg(GetRegisteredComponents)
      probe.reply(RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3")))

      testRouteResult ~> check {
        responseAs[RegisteredComponents] shouldBe
          RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3"))
      }
    }

    "handle a GET on /components/<known componentId> with details of that component" in new AdministratorWithProbe() {
      val componentId = "knownId"
      val routeTestResult = Get(s"/components/$componentId") ~> routes

      probe.expectMsgPF() { case GetMetrics(`componentId`) => true }
      probe.reply(Details(Nil))

      routeTestResult ~> check {
        responseAs[Details] shouldBe Details(Nil)
      }
    }

    "handle a GET on /components/<known componentId>/metrics with details of that component" in new AdministratorWithProbe() {
      val componentId = "knownId2"
      val routeTestResult = Get(s"/components/$componentId/metrics") ~> routes

      probe.expectMsgPF() { case GetMetrics(`componentId`) => true }
      probe.reply(Details(Nil))

      routeTestResult ~> check {
        responseAs[Details] shouldBe Details(Nil)
      }
    }

    "handle a POST on /components by creating a new registered componentId" in new AdministratorWithProbe() {
      val routeTestResult = Post("/components", RegisterComponent("RegisteredComponent1")) ~> routes

      probe.expectMsg(RegisterComponent("RegisteredComponent1"))
      probe.reply(LogParserCreated("RegisteredComponent1"))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.Created
      }
    }

    "handle a POST on /components/logParserActor by creating a new registered componentId" in new AdministratorWithProbe() {
      val componentId = "bla"
      val metric = Metric("key", "+d")
      val routeTestResult = Post(s"/components/$componentId", Metric("key", "+d")) ~> routes

      probe.expectMsg(RegisterMetric(componentId, metric))
      probe.reply(MetricRegistered(metric))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.Created
      }
    }
  }
}


