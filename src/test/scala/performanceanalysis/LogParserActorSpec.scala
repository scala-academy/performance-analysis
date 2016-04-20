package performanceanalysis

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol.{Details, Metric, MetricRegistered, RequestDetails}

class LogParserActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {

  def this() = this(ActorSystem("AdministratorActorSpec"))

  "LogParserActor" must {

    "send metrics on request for details" in {
      val logParserActorRef = TestActorRef(new LogParserActor())
      logParserActorRef ! RequestDetails
      expectMsg(Details(Nil))
    }

    "add a metric to it's state" in {
      val logParserActorRef = TestActorRef(new LogParserActor())
      val metric = Metric("key", "some regex")
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))
      logParserActorRef ! RequestDetails
      expectMsg(Details(List(metric)))
    }

  }

}
