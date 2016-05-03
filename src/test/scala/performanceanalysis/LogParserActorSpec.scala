package performanceanalysis

import akka.actor.{ActorSystem, UnhandledMessage}
import akka.testkit.TestActorRef
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol.{LogSubmitted, _}

class LogParserActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {

  def this() = this(ActorSystem("AdministratorActorSpec"))

  "LogParserActor" must {
    val logParserActorRef = TestActorRef(new LogParserActor())
    val metric = Metric("key", """some (\d+)""")

    "send metrics on request for details" in {
      logParserActorRef ! RequestDetails
      expectMsg(Details(Nil))
    }

    "add a metric to it's state" in {
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))
      logParserActorRef ! RequestDetails
      expectMsg(Details(List(metric)))
    }

    "add a parsed logs to it's state" in {
      system.eventStream.subscribe(testActor, classOf[UnhandledMessage])
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))
      val message: LogSubmitted = LogSubmitted("compId", "some 123 some 345 some 567")
      logParserActorRef ! message
      expectNoMsg()
    }
  }

}
