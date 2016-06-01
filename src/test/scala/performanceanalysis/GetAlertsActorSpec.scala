package performanceanalysis

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol
import performanceanalysis.server.Protocol.Rules.{AlertRule, Action => RuleAction}
import performanceanalysis.server.Protocol._

class GetAlertsActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {

  def this() = this(ActorSystem("GetAlertsActorSpec"))

  "GetAlertsActor" must {

    "send RequestAlertRuleDetails to ruleActors" in {
      val senderProbe = TestProbe()
      val ruleActor = TestProbe()
      val rules = List[ActorRef](ruleActor.testActor)
      TestActorRef(new GetAlertsActor(rules, senderProbe.testActor))

      ruleActor.expectMsg(RequestAlertRuleDetails)
    }

    "send the result back and stop itself when all ruleActors have replied" in {
      val senderProbe = TestProbe()
      val ruleActorProbe1 = TestProbe()
      val ruleActorProbe2 = TestProbe()
      val rules = List[ActorRef](ruleActorProbe1.testActor, ruleActorProbe2.testActor)

      val ref = TestActorRef(new GetAlertsActor(rules, senderProbe.testActor))

      senderProbe.watch(ref)

      val alertRule1 = AlertRule("_ < 100", Protocol.Rules.Action("url1"))
      ruleActorProbe1.expectMsg(RequestAlertRuleDetails)
      ruleActorProbe1.reply(SingleAlertRuleDetails(alertRule1))

      senderProbe.expectNoMsg()

      val alertRule2 = AlertRule("_ < 100", Protocol.Rules.Action("url2"))
      ruleActorProbe2.expectMsg(RequestAlertRuleDetails)
      ruleActorProbe2.reply(SingleAlertRuleDetails(alertRule2))

      senderProbe.expectMsg(AllAlertRuleDetails(Set[AlertRule](alertRule1, alertRule2)))
      senderProbe.expectTerminated(ref)
    }
  }
}
