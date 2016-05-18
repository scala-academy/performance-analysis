package performanceanalysis.administrator

import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestProbe
import performanceanalysis.base.SpecBase
import performanceanalysis.server.Protocol.Rules.{Action, AlertRule, Threshold}
import performanceanalysis.server.Protocol._

/**
  * Created by Jordi on 9-3-2016.
  */
class AdministratorSpec extends SpecBase with ScalatestRouteTest {

  class AdministratorWithProbe extends Administrator(system.deadLetters) {

    val probe = TestProbe()

    override protected val administratorActor = probe.ref
  }

  trait TestConstants {
    val knownId = "knownId"
    val unknownId = "unknownId"
    val knownKey = "knownKey"
    val unknownKey = "unknownKey"
  }

  class TestAdministrator()

  "The server" must {
    "create an administrator actor and route messages to it" in new AdministratorWithProbe() {
      val routeTestResult = Get("/components") ~> routes

      probe.expectMsg(GetRegisteredComponents)
      probe.reply(RegisteredComponents(Set()))

      routeTestResult ~> check {
        status shouldBe StatusCodes.OK
        responseAs[RegisteredComponents] shouldBe RegisteredComponents(Set())
      }
    }

    "handle a GET on /components by returning all registered componentIds" in new AdministratorWithProbe() {
      val testRouteResult = Get("/components") ~> routes

      probe.expectMsg(GetRegisteredComponents)
      probe.reply(RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3")))

      testRouteResult ~> check {
        responseAs[RegisteredComponents] shouldBe
          RegisteredComponents(Set("RegisteredComponent1", "RegisteredComponent2", "RegisteredComponent3"))
      }
    }

    "handle a GET on /components/<known componentId>/metrics with details of that component" in new AdministratorWithProbe() {
      val componentId = "knownId2"
      val routeTestResult = Get(s"/components/$componentId/metrics") ~> routes

      probe.expectMsgPF() { case GetDetails(`componentId`) => true }
      probe.reply(Details(Nil))

      routeTestResult ~> check {
        responseAs[Details] shouldBe Details(Nil)
      }
    }

    "handle a GET on /components/<known componentID>/metrics/<known metricKey>/alerting-rules" in new AdministratorWithProbe with TestConstants {
      val routeTestResult = Get(s"/components/$knownId/metrics/$knownKey/alerting-rules") ~> routes

      val answer = AlertRulesDetails(Set[AlertRule](AlertRule(Threshold("t"), Action("a"))))
      probe.expectMsg(GetAlertRules(knownId, knownKey))
      probe.reply(answer)

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.OK
        responseAs[AlertRulesDetails] shouldBe answer
      }
    }

    "handle a DELETE on /components/<known componentID>/metrics/<known metricKey>/alerting-rules by sending delete message" in
        new AdministratorWithProbe with TestConstants {
      val routeTestResult = Delete(s"/components/$knownId/metrics/$knownKey/alerting-rules") ~> routes

      probe.expectMsg(DeleteAllAlertingRules(knownId, knownKey))
      probe.reply(AlertRulesDeleted(knownId))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "handle a DELETE on a non-existing metric by sending 404" in new AdministratorWithProbe with TestConstants {
      val routeTestResult = Delete(s"/components/$knownId/metrics/$unknownKey/alerting-rules") ~> routes

      probe.expectMsg(DeleteAllAlertingRules(knownId, unknownKey))
      probe.reply(MetricNotFound(knownId, unknownKey))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.NotFound
      }
    }

    "handle a DELETE on a metric with no alerts by sending 203" in new AdministratorWithProbe with TestConstants {
      val routeTestResult = Delete(s"/components/$knownId/metrics/$knownKey/alerting-rules") ~> routes

      probe.expectMsg(DeleteAllAlertingRules(knownId, knownKey))
      probe.reply(NoAlertsFound(knownId, knownKey))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "handle a POST on /components by creating a new registered componentId" in new AdministratorWithProbe() {
      val componentId = "RegisteredComponent1"
      val routeTestResult = Post("/components/metrics", RegisterComponent(componentId)) ~> routes

      probe.expectMsg(RegisterComponent(componentId))
      probe.reply(LogParserCreated(componentId))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.Created
      }
    }

    "handle a POST on /components/logParserActor by creating a new registered componentId" in new AdministratorWithProbe() {
      val componentId = "bla"
      val metric = Metric("key", "+d")
      val routeTestResult = Post(s"/components/$componentId/metrics", Metric("key", "+d")) ~> routes

      probe.expectMsg(RegisterMetric(componentId, metric))
      probe.reply(MetricRegistered(metric))

      routeTestResult ~> check {
        response.status shouldBe StatusCodes.Created
      }
    }

    "handle a POST on /components/<component>/metrics/<mkey>/alerting-rules by creating a new alerting rule" in new AdministratorWithProbe() {
      val rule = AlertRule(Threshold("2000 millis"), Action("dummy-action"))
      val routeTestResult = Post("/components/cid/metrics/mkey/alerting-rules", rule) ~> routes

      probe.expectMsg(RegisterAlertRule("cid", "mkey", rule))
      probe.reply(AlertRuleCreated("cid", "mkey", rule))

      routeTestResult ~> check {
        response.status shouldBe Created
      }
    }
  }
}
