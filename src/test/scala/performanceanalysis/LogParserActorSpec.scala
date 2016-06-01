package performanceanalysis

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.Rules.{AlertRule, Threshold, Action => RuleAction}
import performanceanalysis.server.Protocol._

import scala.concurrent.duration._
import scala.language.postfixOps


class LogParserActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("LogParserActorSpec"))

  trait TestSetup {
    val defaultAlertRuleActorProbe = TestProbe("defaultAlertRuleActor")
    val alertRule1ActorProbe = TestProbe("alertRule1Actor")
    val alertRule2ActorProbe = TestProbe("alertRule2Actor")
    val logParserActorRef = TestActorRef(new LogParserActor() with TestAlertRuleActorCreator)
    val alertingRule = AlertRule(Threshold("2000 ms"), RuleAction("aUrl"))

    trait TestAlertRuleActorCreator extends AlertRuleActorCreator {
      override def create(context: ActorContext, rule: AlertRule, componentId: String, metricKey: String): ActorRef = {
        rule match {
          case AlertRule(_, RuleAction("aUrlForRule1")) => alertRule1ActorProbe.ref
          case AlertRule(_, RuleAction("aUrlForRule2")) => alertRule2ActorProbe.ref
          case _ => defaultAlertRuleActorProbe.ref
        }
      }
    }

    def sendMetricAndAssertResponse(metric: Metric): Unit = {
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))
    }

    def registerAlertRule(key: MetricKey, rule : AlertRule):Unit = {
      logParserActorRef ! RegisterAlertRule("aCid", key, rule)
      expectMsg(AlertRuleCreated("aCid", key, rule))
    }
  }

  val componentId = "cid"
  val metricKey1: MetricKey = "aMetricKey1"
  val metricKey2: MetricKey = "aMetricKey2"

  trait TestSetupWithMetricRegistered extends TestSetup {

    def alertingRule(ruleAction: String): AlertRule = {
      val someThreshold: Threshold = Threshold("1800 ms")
      AlertRule(someThreshold, RuleAction(ruleAction))
    }

    sendMetricAndAssertResponse(Metric(metricKey1, """(\d+ ms)""", ValueType(classOf[Duration]))) //good regex
    sendMetricAndAssertResponse(Metric(metricKey2, """\d+ ms""", ValueType(classOf[Duration]))) //group doesn't exist, so it is bad regex
  }

  trait TestSetupWithAlertsRegistered extends TestSetupWithMetricRegistered {
    val rules = List(AlertRule(Threshold("2001 ms"), RuleAction("aUrlForRule1")),
      AlertRule(Threshold("2002 ms"), RuleAction("aUrlForRule2")))
    for (rule <- rules) {
      registerAlertRule(metricKey1, rule)
    }

    registerAlertRule(metricKey2, rules.head)
  }

  "LogParserActor" must {

    "send metrics on request for details" in new TestSetup {
      val logParserActor = TestActorRef(LogParserActor.props)
      logParserActor ! RequestDetails
      expectMsg(Details(Nil))
    }

    "add a metric to it's state" in new TestSetup {
      val metric = Metric("key", "some regex")
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))

      logParserActorRef ! RequestDetails
      expectMsg(Details(List(metric)))
    }

    "send message MetricNotFound when no metric for given alerting rule found" in new TestSetup {
      logParserActorRef ! RegisterAlertRule(componentId, metricKey1, alertingRule)
      expectMsg(MetricNotFound(componentId, metricKey1))
    }

    "send message AlertingRuleCreated when alerting rule is registered" in new TestSetupWithMetricRegistered {
      registerAlertRule(metricKey1, alertingRule("aUrl"))
    }

    "send message to AlertRuleActors when log submitted" in new TestSetupWithMetricRegistered {
      registerAlertRule(metricKey1, alertingRule("aUrlForRule1"))
      registerAlertRule(metricKey2, alertingRule("aUrlForRule2"))

      logParserActorRef ! SubmitLog(componentId, "some action took 2000 ms")
      alertRule1ActorProbe.expectMsg(CheckRuleBreak(2000 millis))
      //regular expression is not matching, so rule should not get message
      alertRule2ActorProbe.expectNoMsg()
    }

    "parse log with boolean metric" in new TestSetup {
      val metric = Metric("key-with-error", "(ERROR)", ValueType(classOf[Boolean]))
      sendMetricAndAssertResponse(metric)

      registerAlertRule(metric.metricKey, alertingRule)

      // submit a log with ERROR in it
      logParserActorRef ! SubmitLog("aCid", "log line with ERROR in it")
      defaultAlertRuleActorProbe.expectMsg(CheckRuleBreak(true))
    }

    "parse log with boolean metric when no match should not trigger a message to AlertRuleActor" in new TestSetup {
      val metric = Metric("key-with-no-error", "(ERROR)", ValueType(classOf[Boolean]))
      sendMetricAndAssertResponse(metric)

      registerAlertRule(metric.metricKey, alertingRule)

      logParserActorRef ! SubmitLog("aCid", "log line with INFO in it")
      defaultAlertRuleActorProbe.expectNoMsg
    }
  }

  "send MetricNotFound when requesting details of non-existing metric" in new TestSetupWithMetricRegistered {
    logParserActorRef ! RequestAlertRules("notAMetricKey")
    expectMsg(MetricNotFound)
  }

  "forward RequestAlertRuleDetails messages to AlertRuleActors when requesting alert rules" in new TestSetupWithAlertsRegistered {
    logParserActorRef ! RequestAlertRules(metricKey1)

    alertRule1ActorProbe.expectMsg(RequestAlertRuleDetails)
    alertRule2ActorProbe.expectMsg(RequestAlertRuleDetails)

    expectNoMsg()
  }

  "reply MetricNotFound when deleting alert rules of a non-existing metric" in new TestSetupWithAlertsRegistered {
    logParserActorRef ! DeleteAllAlertingRules(componentId, "notAMetricKey")
    expectMsg(MetricNotFound(componentId, "notAMetricKey"))
  }

  "reply NoAlertsFound when deleting alert rules of a metric without alerts" in new TestSetupWithMetricRegistered {
    logParserActorRef ! DeleteAllAlertingRules(componentId, metricKey1)
    expectMsg(NoAlertsFound(componentId, metricKey1))
  }

  "handle deletion of all alert rules of a metric" in new TestSetupWithAlertsRegistered {
    val probe = TestProbe()

    probe.watch(alertRule1ActorProbe.ref)
    probe.watch(alertRule2ActorProbe.ref)

    logParserActorRef ! DeleteAllAlertingRules(componentId, metricKey1)

    expectMsg(AlertRulesDeleted(componentId))

    probe.expectTerminated(alertRule2ActorProbe.ref)
    probe.expectTerminated(alertRule1ActorProbe.ref)
  }
}
