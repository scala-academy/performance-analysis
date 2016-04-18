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

  "The server" must {
    "create an administrator actor and route messages to it" in new Administrator(system.deadLetters) {
      val probe = TestProbe()
      override protected val administratorActor = probe.ref

      val routeTestResult = Get("/components") ~> routes

      probe.expectMsg(GetRegisteredComponents)
      probe.reply(RegisteredComponents(Set()))

      routeTestResult ~> check {
        status shouldBe StatusCodes.OK
        responseAs[RegisteredComponents] shouldBe RegisteredComponents(Set())
      }
    }

    "handle a GET on /components by returning all registered componentIds" in new Administrator(system.deadLetters) {
      val probe = TestProbe()
      override protected val administratorActor = probe.ref

      val testRouteResult = Get("/components") ~> routes

      probe.expectMsg(GetRegisteredComponents)
      probe.reply(RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3")))

      testRouteResult ~> check {
        val result = responseAs[RegisteredComponents]
        responseAs[RegisteredComponents] shouldBe
          RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3"))
      }
    }

    "handle a GET on /components/<known componentId> with details of that component" in new Administrator(system.deadLetters) {
      val probe = TestProbe()
      override protected val administratorActor = probe.ref

      val componentId = "knownId"
      val routeTestResult = Get(s"/components/$componentId") ~> routes

      probe.expectMsgPF() {case GetDetails(`componentId`) => true}
      probe.reply(Details(componentId))

      routeTestResult ~> check {
        val result = responseAs[Details]
        result shouldBe Details(componentId)
      }
    }

    "handle a POST on /components by creating a new registered componentId" in new Administrator(system.deadLetters) {
      val probe = TestProbe()
      override protected val administratorActor = probe.ref

      val routeTestResult = Post("/components", RegisterComponent("RegisteredComponent1")) ~> routes

      probe.expectMsg(RegisterComponent("RegisteredComponent1"))
      probe.reply(LogParserCreated("RegisteredComponent1"))

      routeTestResult ~>  check {
        response.status shouldBe StatusCodes.Created
      }
    }
  }
}


