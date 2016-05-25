package performanceanalysis.server

import performanceanalysis.server.InterActorMessage.Details
import performanceanalysis.server.Protocol.Metric
import spray.json.DefaultJsonProtocol

/**
  * Created by janwillem on 18/05/16.
  */
object InterActorMessage {

  /**
    * Used by LogParserActor to trigger an alert action check. Message handled by AlerRuleActor.
    */
  case class CheckRuleBreak(value: String)

  /**
    * Used by ActionAlertActor to trigger an action when a rule breaks. Handled by AlertActionActor.
    */
  case class Action(url: String, message: String)

  /**
    * Used by LogParserActor towards AdministratorActor to return its details
    */
  case class Details(metrics: List[Metric])

}