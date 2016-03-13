package placeholder.base

import java.net.InetSocketAddress

import com.twitter.finagle
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.scalatest._
import placeholder.Main

import scala.concurrent.Await
import scala.concurrent.duration._

trait IntegrationTestBase extends FeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {
  val main = Main

  var adminServerAddress: InetSocketAddress = _
  var adminRequestHost: String = _
  var adminClient: Service[Request, Response] = _

  var gathererServerAddress: InetSocketAddress = _
  var gathererRequestHost: String = _
  var gathererClient: Service[Request, Response] = _

  main.main(Array())

  override def beforeAll(): Unit = {
    adminServerAddress = Await.result(main.getAdministratorAddress, 10.seconds)
    adminRequestHost = s"localhost:${adminServerAddress.getPort.toString}"
    adminClient = finagle.Http.newService(adminRequestHost)

    gathererServerAddress = Await.result(main.getGathererAddress, 10.seconds)
    gathererRequestHost = s"localhost:${gathererServerAddress.getPort.toString}"
    gathererClient = finagle.Http.newService(gathererRequestHost)
  }

  def performAdminRequest(request: Request): Future[Response] = {
    request.host = adminRequestHost
    adminClient(request)
  }

  def performGathererRequest(request: Request): Future[Response] = {
    request.host = gathererRequestHost
    gathererClient(request)
  }
}
