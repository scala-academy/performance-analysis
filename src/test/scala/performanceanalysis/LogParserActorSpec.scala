package performanceanalysis

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.Rules.{AlertingRule, Threshold, Action => RuleAction}
import performanceanalysis.server.Protocol._

class LogParserActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {

  def this() = this(ActorSystem("LogParserActorSpec"))

  trait TestSetup {
    val defaultAlertRuleActorProbe = TestProbe("defaultAlertRuleActor")
    val alertRule1ActorProbe = TestProbe("alertRule1Actor")
    val alertRule2ActorProbe = TestProbe("alertRule2Actor")
    val logParserActorRef = TestActorRef(new LogParserActor() with TestAlertRuleActorCreator)

    trait TestAlertRuleActorCreator extends AlertRuleActorCreator {
      override def create(context: ActorContext, rule: AlertingRule, componentId: String, metricKey: String): ActorRef = {
        rule match {
          case AlertingRule(_, RuleAction("aUrlForRule1")) => alertRule1ActorProbe.ref
          case AlertingRule(_, RuleAction("aUrlForRule2")) => alertRule2ActorProbe.ref
          case _ => defaultAlertRuleActorProbe.ref
        }
      }
    }
  }

  trait TestSetupWithMetricRegistered extends TestSetup {
    val metric = Metric("aMetricKey", "\\d+\\sms")
    logParserActorRef ! metric
    expectMsg(MetricRegistered(metric))
  }

  "LogParserActor" must {

    "send metrics on request for details" in new TestSetup {
      logParserActorRef ! RequestDetails
      expectMsg(Details(Nil))
    }

    "add a metric to it's state" in new TestSetup {
      val metric = Metric("key", "some regex")
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))
      logParserActorRef ! RequestDetails
      expectMsg(Details(List(metric)))
    }

    "send message MetricNotFound when no metric for given altering rule found" in new TestSetup {
      logParserActorRef ! RegisterAlertingRule("aCid", "aMetricKey", AlertingRule(Threshold("2000 ms"), RuleAction("aUrl")))
      expectMsg(MetricNotFound("aCid", "aMetricKey"))
    }

    "send message AlertingRuleCreated when alterting rule can be registered" in new TestSetupWithMetricRegistered {
      val rule = AlertingRule(Threshold("2000 ms"), RuleAction("aUrl"))
      logParserActorRef ! RegisterAlertingRule("aCid", "aMetricKey", rule)
      expectMsg(AlertingRuleCreated("aCid", "aMetricKey", rule))
    }

    "send message to AlertRuleActors when log submitted" in new TestSetupWithMetricRegistered {
      val rules = List(AlertingRule(Threshold("2001 ms"), RuleAction("aUrlForRule1")),
        AlertingRule(Threshold("2002 ms"), RuleAction("aUrlForRule2")))
      for (rule <- rules) {
        logParserActorRef ! RegisterAlertingRule("aCid", "aMetricKey", rule)
        expectMsg(AlertingRuleCreated("aCid", "aMetricKey", rule))
      }

      logParserActorRef ! SubmitLogs("aCid", "some action took 2000 ms")
      alertRule1ActorProbe.expectMsg(CheckRuleBreak("2000 ms"))
      alertRule2ActorProbe.expectMsg(CheckRuleBreak("2000 ms"))
    }
  }

}
