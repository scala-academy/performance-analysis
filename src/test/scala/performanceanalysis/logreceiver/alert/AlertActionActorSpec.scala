package performanceanalysis.logreceiver.alert

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol._
import akka.http.scaladsl.{Http, HttpExt}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.stream.ActorMaterializer
//import org.scalamock.scalatest.MockFactory
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.verify
import org.mockito.Mockito.times
import org.mockito.Matchers.any

class AlertActionActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) with MockitoSugar{
  def this() = this(ActorSystem("AlertActionActorSpec"))



  var alertMethodCalled = false

  var httpMock = mock[HttpExt]
  // define mock on this level to ensure it is accessible in the testcase

  implicit val materializer = ActorMaterializer()(system)

  trait TestTrait {
    this: AlertActionActor =>


    override lazy val http = httpMock

/*    override def alert(endpoint: String, message: String): Unit = {
      alertMethodCalled = true
    }*/
  }

  "AlertActionActor" must {
    val alertActionActor = system.actorOf(Props(new AlertActionActor with TestTrait))

    "send out an alert to a given endpoint when it receives an AlertingRuleViolated message " in {
      val testProbe = TestProbe("testProbe")

      val endpoint = "http://outlaw.net"
      testProbe.send(alertActionActor, AlertRuleViolated(endpoint, "One of your loglines violated an alert rule"))

      val req: HttpRequest = HttpRequest(method = HttpMethods.POST, uri = endpoint)
      verify(httpMock, times(1)).singleRequest(any[HttpRequest])(any[ActorMaterializer])

      testProbe.expectNoMsg()
      
    }
  }
}