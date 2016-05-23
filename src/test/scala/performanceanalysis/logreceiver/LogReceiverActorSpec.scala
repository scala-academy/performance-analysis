package performanceanalysis.logreceiver

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, TestActorRef, TestProbe}
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol._

/**
  * Created by Jordi on 5-4-2016.
  */
class LogReceiverActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("LogReceiverActorSpec"))

  "LogReceiverActor" must {
    val compId = "newComponent1"
    val logParserProbe = TestProbe("logParserProbe")

    "handle incoming RegisterNewLogParser messages by adding the new actor to its context" in {
      val logReceiverActor = TestActorRef(LogReceiverActor.props)
      val testProbe = TestProbe("testProbe")
      val logLines = "log line1\nlog line2"

      // Verify that LogParserNotFound is returned when posting a log on an unknown component
      testProbe.send(logReceiverActor, SubmitLogs(compId, logLines))
      testProbe.expectMsgPF() { case LogParserNotFound(`compId`) => true }

      // Register a new actor and verify that logs are now accepted for processing
      testProbe.send(logReceiverActor, RegisterNewLogParser(compId, logParserProbe.ref))
      EventFilter.debug(start = s"New LogParser created with $compId", occurrences = 1) intercept {
        logReceiverActor ! RegisterNewLogParser(compId, logParserProbe.ref)
      }

      testProbe.send(logReceiverActor, SubmitLogs(compId, logLines))

      logParserProbe.expectMsg(SubmitLog(compId, "log line1"))
      logParserProbe.expectMsg(SubmitLog(compId, "log line2"))

      testProbe.expectMsg(LogsSubmitted)
    }
  }
}
