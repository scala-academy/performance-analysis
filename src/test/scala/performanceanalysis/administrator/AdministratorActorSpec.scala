package performanceanalysis.administrator

import akka.actor._
import akka.testkit.{TestActor, TestProbe}
import performanceanalysis.LogParserActor.{Details, RequestDetails}
import performanceanalysis.administrator.AdministratorActor._
import performanceanalysis.base.ActorSpecBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by m06f791 on 24-3-2016.
  */
class AdministratorActorSpec(_system: ActorSystem) extends ActorSpecBase(_system) {
  def this() = this(ActorSystem("AdministratorActorSpec"))

  "AdministratorActor" must {
    val adminActor = system.actorOf(Props(new AdministratorActor))
    "respond with LogParserCreated and create a new child actor when an unknown component is registered" in {
      val testProbe = TestProbe("createChild1")
      val componentName = "newComponent1"

      // Register a new component and verify that actor responds with LogParserCreated message
      testProbe.send(adminActor, RegisterComponent(componentName))
      testProbe.expectMsgPF() { case LogParserCreated(`componentName`) => true }

      // Verify that child actor was created
      val childActorName = LogParserActorManager.createActorName(componentName)
      val searchString = s"${adminActor.path}/${childActorName}"
      val childActorSearch = system.actorSelection(searchString).resolveOne()
      val childActorFound = childActorSearch.map { ref => true }.recover { case _: Throwable => false }
      whenReady(childActorFound) { searchResult: Boolean => searchResult shouldBe true }
    }

    "respond with LogParserExisted when a known component is registered" in {
      val testProbe = TestProbe("createChild2")
      val componentName = "newComponent2"

      // Register a new component and verify that actor responds with LogParserCreated message
      testProbe.send(adminActor, RegisterComponent(componentName))
      testProbe.expectMsgPF() { case LogParserCreated(`componentName`) => true }

      // Register the same component again and verify that actor responds with LogParserExisted message
      testProbe.send(adminActor, RegisterComponent(componentName))
      testProbe.expectMsgPF() { case LogParserExisted(`componentName`) => true }
    }
    "respond with LogParserNotFound when details of an unknown component is requested" in {
      val testProbe = TestProbe("createChild3")
      val componentName = "newComponent3"

      // Request details of an unknown component and verify that actor responds with LogParserNotFound message
      testProbe.send(adminActor, GetDetails(componentName))
      testProbe.expectMsgPF() { case LogParserNotFound(`componentName`) => true }
    }

  }

  "AdministratorActor (with testprobe as child)" must {
    val componentTestProbe = TestProbe("childactor")
    val testComponentId = "newComponent4"
    componentTestProbe.setAutoPilot(new TestActor.AutoPilot {
      def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = {
        sender ! Details(testComponentId)
        TestActor.NoAutoPilot
      }
    })
    trait TestLogParserActorManager extends LogParserActorManager {
      this: ActorLogging =>
      override def createLogParserActor(context: ActorContext, componentId: String): ActorRef = componentTestProbe.ref

      override def findLogParserActor(context: ActorContext, componentId: String): Future[Option[ActorRef]] = Future(Some(componentTestProbe.ref))
    }
    val adminActor = system.actorOf(Props(new AdministratorActor with TestLogParserActorManager))
    "request a LogHandlerActor for details and forward the result to the requester" in {
      val testProbe = TestProbe("testProbe")
      testProbe.send(adminActor, GetDetails(testComponentId))
      componentTestProbe.expectMsg(RequestDetails)
      testProbe.expectMsgPF() { case Details(`testComponentId`) => true }
    }
  }
}
