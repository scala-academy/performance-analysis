package performanceanalysis.administrator

import akka.actor._
import akka.pattern.{ask, pipe}
import performanceanalysis.server.Protocol._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by m06f791 on 24-3-2016.
  */

object AdministratorActor {

  def props(logReceiverActor: ActorRef): Props = Props(new AdministratorActor(logReceiverActor))
}

class AdministratorActor(logReceiverActor: ActorRef) extends Actor with ActorLogging with LogParserActorCreater {
  this: LogParserActorCreater =>

  def receive: Receive = normal(Map.empty[String, ActorRef])

  def normal(logParserActors: Map[String, ActorRef]): Receive = {
    case RegisterComponent(componentId) =>
      handleRegisterComponent(logParserActors, componentId, sender)
    case GetDetails(componentId) =>
      handleGetDetails(logParserActors, componentId, sender)
    case GetRegisteredComponents =>
      handleGetComponents(logParserActors, sender)
    case RegisterMetric(componentId, metric) =>
      handleRegisterMetric(logParserActors, componentId, metric, sender)
    case msg:RegisterAlertingRule =>
      handleRegisterAlertingRule(logParserActors, msg);
    case msg:DeleteAllAlertingRules =>
      handleDeleteAlertingRules(logParserActors, msg)
    case msg:GetAlertRules =>
      handleGetAlertRules(logParserActors, msg)
  }

  private def handleRegisterComponent(logParserActors: Map[String, ActorRef], componentId: String, sender: ActorRef) = {
    logParserActors.get(componentId) match {
      case None =>
        val newActor = createLogParserActor(context, componentId)
        // Notify LogReceiver of new actor
        logReceiverActor ! RegisterNewLogParser(componentId, newActor)
        // Update actor state
        log.debug(s"Created new component $componentId")
        val newLogParserActors = logParserActors.updated(componentId, newActor)
        context.become(normal(newLogParserActors))
        // Respond to sender
        sender ! LogParserCreated(componentId)
      case Some(ref) =>
        log.debug(s"Actor with component $componentId already existed")
        sender ! LogParserExisted(componentId)
    }
  }

  private def handleGetComponents(logParserActors: Map[String, ActorRef], sender: ActorRef) = {
    sender ! RegisteredComponents(logParserActors.keySet)
  }

  private def routeToLogParser(logParserActors: Map[String, ActorRef], componentId: String, sender: ActorRef)(action: ActorRef => Unit) = {
    logParserActors.get(componentId) match {
      case None => sender ! LogParserNotFound(componentId)
      case Some(ref) => action(ref)
    }
  }

  private def handleGetDetails(logParserActors: Map[String, ActorRef], componentId: String, sender: ActorRef) = {
    routeToLogParser(logParserActors, componentId, sender) { ref =>
      log.debug("Requesting details from {}", ref.path)
      (ref ? RequestDetails) pipeTo sender
    }
  }

  private def handleGetAlertRules(logParserActors: Map[String, ActorRef], msg: GetAlertRules) = {
    routeToLogParser(logParserActors, msg.componentId, sender()) { ref =>
      log.debug("Requesting alert rules from {}", ref.path)
      (ref ? RequestAlertRules(msg.metricKey)) pipeTo sender()
    }
  }

  private def handleRegisterMetric(logParserActors: Map[String, ActorRef], componentId: String, metric: Metric, sender: ActorRef) = {
    routeToLogParser(logParserActors, componentId, sender) { ref =>
        log.debug(s"Sending metric registration to {}", ref.path)
        (ref ? metric) pipeTo sender
    }
  }

  private def handleRegisterAlertingRule(logParserActors: Map[String, ActorRef], msg: RegisterAlertingRule) = {
    routeToLogParser(logParserActors, msg.componentId, sender()) { ref =>
      log.debug("Sending new alerting rule to {}", ref.path)
      (ref ? msg) pipeTo sender()
    }
  }

  private def handleDeleteAlertingRules(logParserActors: Map[String, ActorRef], msg: DeleteAllAlertingRules) = {
    routeToLogParser(logParserActors, msg.componentId, sender()) { ref =>
      log.debug("Sending rule deletion message to {}", ref.path)
      (ref ? msg) pipeTo sender()
    }
  }
}
