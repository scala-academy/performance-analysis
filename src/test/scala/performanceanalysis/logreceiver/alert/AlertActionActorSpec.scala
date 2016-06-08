package performanceanalysis.logreceiver.alert

import akka.actor.{ActorSystem, Props}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.{HttpExt, HttpsConnectionContext}
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestProbe
import com.typesafe.config.ConfigFactory
import performanceanalysis.base.ActorSpecBase
import performanceanalysis.server.Protocol._

import scala.concurrent.Future

//import org.scalamock.scalatest.MockFactory

class AlertActionActorSpec(testSystem: ActorSystem) extends ActorSpecBase(testSystem) {
  def this() = this(ActorSystem("AlertActionActorSpec"))

  val alertMethodCalled = false

  "AlertActionActor" must {

    val endpoint = "http://outlaw.net"
    val req: HttpRequest = HttpRequest(method = HttpMethods.POST, uri = endpoint)

    val alertActionActor = system.actorOf(Props(new AlertActionActor {
      override lazy val http: HttpExt = new HttpExt(ConfigFactory.load()) {
        override def singleRequest(request: HttpRequest,
                                   connectionContext: HttpsConnectionContext,
                                   settings: ConnectionPoolSettings,
                                   log: LoggingAdapter)
                                  (implicit fm: Materializer): Future[HttpResponse] = {
          if (request != req) {
            throw new UnsupportedOperationException
          } else {
            super.singleRequest(request, connectionContext, settings, log)
          }
        }
      }
    }))

    "send out an alert to a given endpoint when it receives an AlertingRuleViolated message " in {
      val testProbe = TestProbe("testProbe")

      testProbe.send(alertActionActor, AlertRuleViolated(endpoint, "One of your loglines violated an alert rule"))

      testProbe.expectNoMsg()
    }
  }
}