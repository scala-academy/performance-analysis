package performanceanalysis.logreceiver.alert

import akka.actor.{ActorContext, ActorRef}

trait AlertActionActorCreator {

  def create(context: ActorContext): ActorRef = context.actorOf(AlertActionActor.props())
}
