package placeholder.administrator

import akka.actor.ActorRef
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.{TestActor, TestProbe}
import placeholder.LogParserActor.Details
import placeholder.Status
import placeholder.administrator.AdministratorActor.{GetDetails, GetRegisteredComponents, RegisteredComponents}
import placeholder.base.SpecBase

/**
  * Created by Jordi on 9-3-2016.
  */
class AdministratorSpec extends SpecBase with ScalatestRouteTest {

  /*
    * Override createActorSystem from ScalatestRouteTest to inject our own system
    */
  "The server" must {
    "handle a GET on /status and return a positive uptime in milliseconds" in new Administrator {
      Get("/status") ~> routes ~> check {
        val status = responseAs[Status]
        val uptime = status.uptime
        val (time, millisStr) = uptime.splitAt(uptime.indexOf(" "))
        assert(time.toInt > 0)
        assert(millisStr === " milliseconds")
      }
    }

    "create an administrator actor and route messages to it" in new Administrator {
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

    "handle a GET on /components by returning all registered componentIds" in new Administrator {
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

    "handle a GET on /components/<known componentId> with details of that component" in new Administrator {
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
  }
}


