package performanceanalysis.logreceiver.alert

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.InterActorMessage.{Action, CheckRuleBreak}
import performanceanalysis.server.Protocol.Rules.{AlertingRule, Threshold, Action => RuleAction}
import performanceanalysis.server.Protocol._


class AlertRuleActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("AlertRuleActorSpec"))

  "An AlertRuleActor" must {

    val alertActionActorProbe = TestProbe("alertActionActor")
    val testProbe = TestProbe("testProbe")

    val rule = AlertingRule(Threshold("2000 ms"), RuleAction("aUrl"))
    val alertRuleActor = system.actorOf(Props(new AlertRuleActor(rule, "aCid", "aMetricKey") with TestAlertActionActorCreator))

    "trigger an action when incoming value breaks the given rule" in {
      testProbe.send(alertRuleActor, CheckRuleBreak("2001 ms"))

      alertActionActorProbe.expectMsg(Action("aUrl", s"Rule $rule was broken for component id aCid and metric key aMetricKey"))
    }

    "NOT trigger an action when incoming does not break the given rule" in {
      testProbe.send(alertRuleActor, CheckRuleBreak("2000 ms"))

      alertActionActorProbe.expectNoMsg()
    }

    trait TestAlertActionActorCreator extends AlertActionActorCreator {
      override def create(context: ActorContext): ActorRef = alertActionActorProbe.ref
    }
  }
}
