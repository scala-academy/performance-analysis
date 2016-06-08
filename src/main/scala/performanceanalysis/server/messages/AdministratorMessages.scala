package performanceanalysis.server.messages

object AdministratorMessages {

  /**
    * Used by Administrator towards AdministratorActor to register a new component
    */
  case class RegisterComponent(componentId: String, dateFormat: Option[String] = None)

  /**
    * Used by AdministratorActor towards Administrator to signal that a log parser was created
    */
  case class LogParserCreated(componentId: String)

  /**
    * Used by AdministratorActor towards Administrator to signal that the component was already registered
    */
  case class LogParserExisted(componentId: String)

  /**
    * Used by AdministratorActor towards Administrator and by LogReceiverActor towards LogReceiver
    * to signal that the component could not be found
    */
  case class LogParserNotFound(componentId: String)

  /**
    * Used by Administrator towards AdministratorActor to request details of a component
    */
  case class GetDetails(componentId: String)

  /**
    * Used by Administrator towards AdministratorActor to request a list of all registered components
    */
  case object GetRegisteredComponents

  /**
    * Used by AdministratorActor towards Administrator to return a list of all registered components
    */
  case class RegisteredComponents(componentIds: Set[String])

}
