package performanceanalysis

import java.lang.management.ManagementFactory

import akka.actor.{ Props, Actor }

import scala.concurrent.duration._

/**
 * Created by Jordi on 7-3-2016.
 */
object StatusActor {
  def props: Props = Props[StatusActor]
}

class StatusActor extends Actor {

  def receive: Receive = {
    case _ => sender() ! Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString())
  }
}
