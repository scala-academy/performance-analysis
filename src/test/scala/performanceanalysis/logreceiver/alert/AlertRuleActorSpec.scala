package performanceanalysis.logreceiver.alert

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.testkit.TestProbe
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.messages.AlertMessages._
import performanceanalysis.server.messages.Rules.{AlertRule, Threshold, Action => RuleAction}

import scala.concurrent.duration._
import scala.language.postfixOps


class AlertRuleActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("AlertRuleActorSpec"))

  "An AlertRuleActor" must {

    val alertActionActorProbe = TestProbe("alertActionActor")
    val testProbe = TestProbe("testProbe")

    val rule = AlertRule(Threshold("2000 ms"), RuleAction("aUrl"))
    val componentId = "aCid"
    val metricKey = "aMetricKey"
    val alertRuleActor = system.actorOf(Props(new AlertRuleActor(rule, componentId, metricKey) with TestAlertActionActorCreator))

    "trigger an action when incoming value breaks the given rule" in {
      val duration = Duration("2001 millis")
      testProbe.send(alertRuleActor, CheckRuleBreak(duration))

      val ruleMsg = s"Rule $rule was broken for component id $componentId and metric key $metricKey with value $duration"
      alertActionActorProbe.expectMsg(Action("aUrl", ruleMsg))
    }

    "NOT trigger an action when incoming does not break the given rule" in {
      testProbe.send(alertRuleActor, CheckRuleBreak(2000 millis))

      alertActionActorProbe.expectNoMsg()
    }

    trait TestAlertActionActorCreator extends AlertActionActorCreator {
      override def create(context: ActorContext): ActorRef = alertActionActorProbe.ref
    }
  }
}
