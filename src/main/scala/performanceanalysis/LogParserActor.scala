package performanceanalysis

import akka.actor.{Actor, ActorLogging, Props}
import performanceanalysis.server.Protocol.{Details, Metric, MetricRegistered, RequestDetails}

/**
  * Created by m06f791 on 25-3-2016.
  */
object LogParserActor {

  def props: Props = Props(new LogParserActor())
}

class LogParserActor extends Actor with ActorLogging {

  def receive: Receive = normal(Nil)

  def normal(metrics: List[Metric]): Receive = {
    case RequestDetails =>
      log.debug("received request for details")
      sender ! Details(metrics)

    case metric: Metric =>
      log.debug(s"received post with metric $metric")
      context.become(normal(metric :: metrics))
      sender ! MetricRegistered(metric)

    case msg => log.debug(s"received $msg in ${self.path}")
  }

}
