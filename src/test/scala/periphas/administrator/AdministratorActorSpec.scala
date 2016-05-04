package periphas.administrator

import akka.actor._
import akka.testkit.{TestActor, TestProbe}
import periphas.base.ActorSpecBase
import periphas.server.Protocol.Rules.{AlertingRule, Threshold, Action => RuleAction}
import periphas.server.Protocol._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by m06f791 on 24-3-2016.
  */
class AdministratorActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("AdministratorActorSpec"))

  "AdministratorActor" must {
    val logReceiverProbe = TestProbe("LogParserProbe")
    val adminActor = system.actorOf(Props(new AdministratorActor(logReceiverProbe.ref)))
    "respond with LogParserCreated and create a new child actor when an unknown component is registered" in {
      val testProbe = TestProbe("createChild1")
      val componentName = "newComponent1"

      // Register a new component and verify that actor responds with LogParserCreated message
      testProbe.send(adminActor, RegisterComponent(componentName))
      testProbe.expectMsgPF() { case LogParserCreated(`componentName`) => true }
      logReceiverProbe.expectMsgPF() { case RegisterNewLogParser(`componentName`, _) => true }

      // Verify that child actor was created
      val childActorName = LogParserActorCreater.createActorName(componentName)
      val searchString = s"${adminActor.path}/$childActorName"
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

    "respond with RegisteredComponents when all components are requested" in {
      val testProbe = TestProbe("getAll")

      // Request all registered components
      testProbe.send(adminActor, GetRegisteredComponents)
      testProbe.expectMsgPF() { case RegisteredComponents(_) => true }
    }

  }

  "AdministratorActor (with testprobe as child)" must {
    val componentTestProbe = TestProbe("childactor")


    trait TestLogParserActorCreater extends LogParserActorCreater {
      this: ActorLogging =>
      override def createLogParserActor(context: ActorContext, componentId: String): ActorRef = componentTestProbe.ref
    }
    val adminActor = system.actorOf(Props(new AdministratorActor(system.deadLetters) with TestLogParserActorCreater))

    "request a LogHandlerActor for details and forward the result to the requester" in {
      val testComponentId = "newComponent4"
      val testProbe = TestProbe("testProbe")
      testProbe.send(adminActor, RegisterComponent(testComponentId))
      testProbe.expectMsgPF() { case LogParserCreated(`testComponentId`) => true }
      testProbe.send(adminActor, GetDetails(testComponentId))
      componentTestProbe.expectMsg(RequestDetails)
      componentTestProbe.reply(Details(Nil))
      testProbe.expectMsgPF() { case Details(Nil) => true }
    }

    "register new alerting rule and foward result to requester" in {
      val testComponentId = "cid"
      val testProbe = TestProbe("administrator")
      testProbe.send(adminActor, RegisterComponent(testComponentId))
      testProbe.expectMsgPF() { case LogParserCreated(`testComponentId`) => true }
      val rule = AlertingRule(Threshold("2000 millis"), RuleAction("dummy-action"))
      val metricKey = "mkey"
      testProbe.send(adminActor, RegisterAlertingRule(testComponentId, `metricKey`, rule))
      componentTestProbe.expectMsgPF() {case RegisterAlertingRule(`testComponentId`, `metricKey`, `rule`) => true}
      componentTestProbe.reply(AlertingRuleCreated(`testComponentId`, `metricKey`, `rule`))
      testProbe.expectMsgPF() { case AlertingRuleCreated(`testComponentId`, `metricKey`, `rule`) => true}
    }
  }
}
