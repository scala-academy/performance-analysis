package performanceanalysis

import akka.actor.{ActorLogging, Actor, Props}

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  case object RequestDetails

  case class Details(componentId: String)

  def props: Props = Props[LogParserActor]
}

class LogParserActor extends Actor with ActorLogging {

  def receive: Receive = {
    case msg => log.debug(s"received $msg in ${self.path}")
  }

}
