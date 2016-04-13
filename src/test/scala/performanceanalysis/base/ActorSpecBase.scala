package performanceanalysis.base

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, NoLogging }
import akka.testkit.{ ImplicitSender, TestKit }
import akka.util.Timeout

class ActorSpecBase(myActorSystem: ActorSystem) extends TestKit(myActorSystem)
    with SpecBase with ImplicitSender {

  protected def log: LoggingAdapter = NoLogging

  protected implicit val timeout: Timeout = Timeout(2, TimeUnit.SECONDS)

  override def afterAll {
    system.shutdown()
  }
}
