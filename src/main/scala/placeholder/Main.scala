package placeholder

import java.net.InetSocketAddress

import scala.concurrent.Future

object Main extends App {

  val administrator = new Administrator {}
  val gatherer = new DataGatherer {}

  def getAdministratorAddress: Future[InetSocketAddress] = administrator.getServerAddress
  def getGathererAddress: Future[InetSocketAddress] = gatherer.getServerAddress
}
