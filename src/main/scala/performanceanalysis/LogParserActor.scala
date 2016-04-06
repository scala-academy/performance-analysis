package performanceanalysis

import akka.actor.{ActorLogging, Actor, Props}

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  def props: Props = Props(new LogParserActor())
}

class LogParserActor extends Actor with ActorLogging {

  def receive: Receive = {
    case msg => log.debug(s"received $msg in ${self.path}")
  }

}
