package performanceanalysis.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import performanceanalysis.server.Protocol._
import performanceanalysis.base.SpecBase

/**
  * Created by Jordi on 9-3-2016.
  */
class AdministratorSpec extends SpecBase with ScalatestRouteTest {

  "The server" must {
    "create an administrator actor and route messages to it" in new Administrator(system.deadLetters) {
      val probe = TestProbe("AdministratorActorProbe")
      probe.setAutoPilot(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          sender ! RegisteredComponents(Set())
          TestActor.NoAutoPilot
        }
      })
      override protected val administratorActor = probe.ref
      Get("/components") ~> routes ~> check {
        probe.expectMsg(GetRegisteredComponents)
      }
    }

    "handle a GET on /components by returning all registered componentIds" in new Administrator(system.deadLetters) {
      val probe = TestProbe("AdministratorActorProbe")
      probe.setAutoPilot(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          sender ! RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3"))
          TestActor.NoAutoPilot
        }
      })
      override protected val administratorActor = probe.ref
      Get("/components") ~> routes ~> check {
        val result = responseAs[RegisteredComponents]
        result shouldBe RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3"))
      }
    }

    "handle a GET on /components/<known componentId> with details of that component" in new Administrator(system.deadLetters) {
      val probe = TestProbe("AdministratorActorProbe")
      val componentId = "knownId"
      probe.setAutoPilot(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          sender ! Details(componentId)
          TestActor.NoAutoPilot
        }
      })
      override protected val administratorActor = probe.ref

      Get(s"/components/$componentId") ~> routes ~> check {
        probe.expectMsgPF() {case GetDetails(`componentId`) => true}
        val result = responseAs[Details]
        result shouldBe Details(componentId)
      }
    }

    "handle a POST on /components by creating a new registered componentId" in new Administrator(system.deadLetters) {
      val probe = TestProbe("AdministratorActorProbe")
      probe.setAutoPilot(new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
          sender ! LogParserCreated("RegisteredComponent1")
          TestActor.NoAutoPilot
        }
      })
      override protected val administratorActor = probe.ref

      Post("/components", RegisterComponent("RegisteredComponent1")) ~> routes ~> check {
        response.status shouldBe StatusCodes.Created
      }
    }
  }
}


