package performanceanalysis.logreceiver

import akka.actor.ActorSystem
import akka.testkit.{EventFilter, TestActorRef, TestProbe}
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.messages.AdministratorMessages._
import performanceanalysis.server.messages.LogMessages._

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
      testProbe.expectMsg(LogParserNotFound(compId))

      // Register a new actor and verify that logs are now accepted for processing
      testProbe.send(logReceiverActor, RegisterNewLogParser(compId, logParserProbe.ref, None))
      EventFilter.info(start = s"New LogParser created with $compId", occurrences = 1) intercept {
        logReceiverActor ! RegisterNewLogParser(compId, logParserProbe.ref, None)
      }

      testProbe.send(logReceiverActor, SubmitLogs(compId, logLines))

      logParserProbe.expectMsg(SubmitLog(compId, "log line1"))
      logParserProbe.expectMsg(SubmitLog(compId, "log line2"))

      testProbe.expectMsg(LogsSubmitted)
    }
  }


  "splitIntoLines" must {
    import LogReceiverActor.splitIntoLines
    val expected = List("line1", "line2", "line3", "   line4", "\tline5")

    "return array of lines if windows EOL exists" in {
      val input = "line1\r\nline2\r\nline3\r\n   line4\r\n\tline5"
      splitIntoLines(input) shouldBe expected
    }

    "return array of lines if unix or OSX EOL exists" in {
      val input = "line1\nline2\nline3\n   line4\n\tline5"
      splitIntoLines(input) shouldBe expected
    }

    "return array of lines if mac EOL exists" in {
      val input = "line1\rline2\rline3\r   line4\r\tline5"
      splitIntoLines(input) shouldBe expected
    }

    "return same input when no EOL" in {
      val input: String = "single line"
      splitIntoLines(input) shouldBe Array(input)
    }
  }
}
