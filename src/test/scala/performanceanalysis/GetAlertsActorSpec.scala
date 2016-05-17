package performanceanalysis

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol
import performanceanalysis.server.Protocol.Rules.{Action => RuleAction, AlertRule, Threshold}
import performanceanalysis.server.Protocol._

class GetAlertsActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {

  def this() = this(ActorSystem("GetAlertsActorSpec"))

  "GetAlertsActor" must {

    "send GetDetails to ruleActors" in {
      val senderProbe = TestProbe()
      val ruleActor = TestProbe()
      val rules = List[ActorRef](ruleActor.testActor)
      TestActorRef(new GetAlertsActor(rules, senderProbe.testActor))

      ruleActor.expectMsg(GetDetails(""))
    }

    "send the result back when all ruleActors have replied" in {
      val senderProbe = TestProbe()
      val ruleActorProbe1 = TestProbe()
      val ruleActorProbe2 = TestProbe()
      val rules = List[ActorRef](ruleActorProbe1.testActor, ruleActorProbe2.testActor)
      TestActorRef(new GetAlertsActor(rules, senderProbe.testActor))

      val alertRule1 = AlertRule(Threshold("max"), Protocol.Rules.Action("url1"))
      ruleActorProbe1.expectMsg(GetDetails(""))
      ruleActorProbe1.reply(AlertRuleDetails(alertRule1))

      senderProbe.expectNoMsg()

      val alertRule2 = AlertRule(Threshold("max"), Protocol.Rules.Action("url2"))
      ruleActorProbe2.expectMsg(GetDetails(""))
      ruleActorProbe2.reply(AlertRuleDetails(alertRule2))

      senderProbe.expectMsg(AlertRulesDetails(Set[AlertRule](alertRule1, alertRule2)))
    }
  }
}
