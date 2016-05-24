package performanceanalysis

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestProbe}
import performanceanalysis.LogParserActor.MetricKey
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.logreceiver.alert.AlertRuleActorCreator
import performanceanalysis.server.Protocol.Rules.{AlertingRule, Action => RuleAction}
import performanceanalysis.server.Protocol._

import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps


class LogParserActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("LogParserActorSpec"))

  trait TestSetup {
    val defaultAlertRuleActorProbe = TestProbe("defaultAlertRuleActor")
    val alertRule1ActorProbe = TestProbe("alertRule1Actor")
    val alertRule2ActorProbe = TestProbe("alertRule2Actor")
    val logParserActorRef = TestActorRef(new LogParserActor() with TestAlertRuleActorCreator)
    val alertingRule = AlertingRule("_ < 2000 ms", RuleAction("aUrl"))

    trait TestAlertRuleActorCreator extends AlertRuleActorCreator {
      override def create(context: ActorContext, rule: AlertingRule, compId: String, metric: Metric): ActorRef = {
        rule match {
          case AlertingRule(_, RuleAction("aUrlForRule1")) => alertRule1ActorProbe.ref
          case AlertingRule(_, RuleAction("aUrlForRule2")) => alertRule2ActorProbe.ref
          case _ => defaultAlertRuleActorProbe.ref
        }
      }
    }

    def sendMetricAndAssertResponse(metric: Metric): Unit = {
      logParserActorRef ! metric
      expectMsg(MetricRegistered(metric))
    }

    def registerAlertingRule(key: MetricKey, rule : AlertingRule):Unit = {
      logParserActorRef ! RegisterAlertingRule("aCid", key, rule)
      expectMsg(AlertingRuleCreated("aCid", key, rule))
    }
  }

  val metricKey1: MetricKey = "aMetricKey1"
  val metricKey2: MetricKey = "aMetricKey2"

  trait TestSetupWithMetricRegistered extends TestSetup {

    def alertingRule(ruleAction: String): AlertingRule = {
      AlertingRule("_ < 1800 ms", RuleAction(ruleAction))
    }

    sendMetricAndAssertResponse(Metric(metricKey1, """(\d+ ms)""", ValueType(classOf[Duration]))) //good regex
    sendMetricAndAssertResponse(Metric(metricKey2, """\d+ ms""", ValueType(classOf[Duration]))) //group doesn't exist, so it is bad regex
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

    "send message MetricNotFound when no metric for given altering rule found" in new TestSetup {
      logParserActorRef ! RegisterAlertingRule("aCid", metricKey1, alertingRule)
      expectMsg(MetricNotFound("aCid", metricKey1))
    }

    "send message AlertingRuleCreated when alerting rule can be registered" in new TestSetupWithMetricRegistered {
      registerAlertingRule(metricKey1, alertingRule("aUrl"))
    }

    "send message to AlertRuleActors when log submitted" in new TestSetupWithMetricRegistered {
      registerAlertingRule(metricKey1, alertingRule("aUrlForRule1"))
      registerAlertingRule(metricKey2, alertingRule("aUrlForRule2"))

      logParserActorRef ! SubmitLog("aCid", "some action took 2000 ms")
      alertRule1ActorProbe.expectMsg(CheckRuleBreak(2000 millis))
      //regular expression is not matching, so rule should not get message
      alertRule2ActorProbe.expectNoMsg()
    }

    "parse log with boolean metric" in new TestSetup {
      val metric = Metric("key-with-error", "(ERROR)", ValueType(classOf[Boolean]))
      sendMetricAndAssertResponse(metric)

      registerAlertingRule(metric.metricKey, alertingRule)

      // submit a log with ERROR in it
      logParserActorRef ! SubmitLog("aCid", "log line with ERROR in it")
      defaultAlertRuleActorProbe.expectMsg(CheckRuleBreak(true))
    }

    "parse log with boolean metric when no match should not trigger a message to AlertRuleActor" in new TestSetup {
      val metric = Metric("key-with-no-error", "(ERROR)", ValueType(classOf[Boolean]))
      sendMetricAndAssertResponse(metric)

      registerAlertingRule(metric.metricKey, alertingRule)

      logParserActorRef ! SubmitLog("aCid", "log line with INFO in it")
      defaultAlertRuleActorProbe.expectNoMsg
    }
  }

}
