package performanceanalysis.logreceiver

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol._

/**
  * Created by Jordi on 5-4-2016.
  */
class LogReceiverActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("LogReceiverActorSpec"))

  "LogReceiverActor" must {
    val logReceiverActor = system.actorOf(LogReceiverActor.props)
    "handle incoming RegisterNewLogParser messages by adding the new actor to its context" in {
      val testProbe = TestProbe("testProbe")
      val logParserProbe = TestProbe("logParserProbe")
      val componentName = "newComponent1"

      // Verify that LogParserNotFound is returned when posting a log on an unknown component
      testProbe.send(logReceiverActor, SubmitLog(componentName, "test log line"))
      testProbe.expectMsgPF() { case LogParserNotFound(`componentName`) => true }

      // Register a new actor and verify that logs are now accepted for processing
      testProbe.send(logReceiverActor, RegisterNewLogParser(componentName, logParserProbe.ref))
      testProbe.send(logReceiverActor, SubmitLog(componentName, "test log line"))
      testProbe.expectMsgPF() { case "OK" => true }
    }
  }
}
