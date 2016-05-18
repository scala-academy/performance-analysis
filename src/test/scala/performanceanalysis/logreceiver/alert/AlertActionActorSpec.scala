package performanceanalysis.logreceiver.alert

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol._

class AlertActionActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("AlertActionActorSpec"))

  "AlertActionActor" must {
    val alertActionActor = system.actorOf(Props[AlertActionActor])

    "send out an alert to a given endpoint when it receives an AlertingRuleViolated message " in {
      val testProbe = TestProbe("testProbe")

      testProbe.send(alertActionActor, AlertRuleViolated("http://somewhere.net", "One of your loglines violated an alert rule"))
      // Here I would like to verify that the alertActionActor sent out a request. Is there a possibility to do so?
      testProbe.expectNoMsg()
    }
  }

}