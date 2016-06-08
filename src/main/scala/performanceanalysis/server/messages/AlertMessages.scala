package performanceanalysis.server.messages

object AlertMessages {

  /**
    * Used by Administrator towards AdministratorActor to request AlertRules of a metric of a component
    */
  case class GetAlertRules(componentId: String, metricKey: String)

  /**
    * Used by AdministratorActor to request AlertRules of a component
    */
  case class RequestAlertRules(metricKey: String)

  /**
    * Used by GetAlertsActor to request details from AlertRuleActor
    */
  case object RequestAlertRuleDetails

  /**
    * Used by AlertRuleActor to send its details to GetAlertsActor
    */
  case class SingleAlertRuleDetails(alertRule: Rules.AlertRule)

  /**
    * Used by GetAlertsActor to send the collected AlertRules to AdministratorActor
    */
  case class AllAlertRuleDetails(alertRules: Set[Rules.AlertRule])

  /**
    * Used by LogParserActor to indicate to AdministratorActor that no Alerts are registered for this metric
    */
  case class NoAlertsFound(componentId: String, metricKey: String)


  /**
    * Used by the Administrator towards AdministratorActor to add a new alerting rule.
    */
  case class RegisterAlertRule(componentId: String, metricKey: String, rule: Rules.AlertRule)

  /**
    * Used by AdministratorActor towards Administrator to indicate that the given rule was successfully created.
    */
  case class AlertRuleCreated(componentId: String, metricKey: String, rule: Rules.AlertRule)

  /**
    * Used by the Administrator towards AdministratorActor to delete all alerting rules
    */
  case class DeleteAllAlertingRules(componentId: String, metricKey: String)

  /**
    * Used by AdministratorActor towards Administrator to indicate that the rules were successfully deleted.
    */
  case class AlertRulesDeleted(componentId: String)

  /**
    * Used by LogParserActor to trigger an alert action check. Message handled by AlertRuleActor.
    */
  case class CheckRuleBreak(value: Any)

  /**
    * Used by ActionAlertActor to trigger an action when a rule breaks. Handled by AlertActionActor.
    */
  case class Action(url: String, message: String)

}
