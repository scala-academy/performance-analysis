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
  val server = Main

  var serverAddress: InetSocketAddress = _
  var client: Service[Request, Response] = _
  var requestHost: String = _

  server.main(Array())

  override def beforeAll(): Unit = {
    serverAddress = Await.result(server.getServerAddress, 10.seconds)
    requestHost = s"localhost:${serverAddress.getPort.toString}"
    client = finagle.Http.newService(requestHost)
  }

  def performRequest(request: Request): Future[Response] = {
    request.host = requestHost
    client(request)
  }
}
