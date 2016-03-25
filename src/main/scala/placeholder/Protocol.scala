package placeholder

import placeholder.LogParserActor.Details
import placeholder.administrator.AdministratorActor.RegisteredComponents
import spray.json.DefaultJsonProtocol

case class Status(uptime: String)

trait Protocol extends DefaultJsonProtocol {
  implicit val statusFormatter = jsonFormat1(Status.apply)
  implicit val detailsFormatter = jsonFormat1(Details.apply)
  implicit val registeredComponentsFormatter = jsonFormat1(RegisteredComponents.apply)
}
