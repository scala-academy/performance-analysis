package placeholder

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import placeholder.base.ActorSpecBase

/**
 * Created by Jordi on 7-3-2016.
 */
class StatusActorSpec(_system: ActorSystem) extends ActorSpecBase(_system) {

  def this() = this(ActorSystem("StatusActor"))

  "StatusActor" must {
    "reply with the uptime of the server" in {
      val statusActor = system.actorOf(StatusActor.props)
      val testProbe = TestProbe("test")

      testProbe.send(statusActor, "dummy message")

      testProbe.expectMsgClass(classOf[Status])
    }
  }

}
