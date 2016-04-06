package performanceanalysis.base

import java.net.InetSocketAddress

import com.twitter.finagle
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, RequestBuilder, Response}
import com.twitter.io.Bufs._
import com.twitter.util.Future
import org.scalatest._
import performanceanalysis.server.Main

import scala.concurrent.Await
import scala.concurrent.duration._

trait IntegrationTestBase extends FeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {
  val main = Main

  var adminServerAddress: InetSocketAddress = _
  var adminRequestHost: String = _
  var adminClient: Service[Request, Response] = _

  var logReceiverServerAddress: InetSocketAddress = _
  var logReceiverRequestHost: String = _
  var logReceiverClient: Service[Request, Response] = _

  main.main(Array())

  override def beforeAll(): Unit = {
    adminServerAddress = Await.result(main.administrator.getServerAddress, 10.seconds)
    adminRequestHost = s"localhost:${adminServerAddress.getPort.toString}"
    adminClient = finagle.Http.newService(adminRequestHost)

    logReceiverServerAddress = Await.result(main.logReceiver.getServerAddress, 10.seconds)
    logReceiverRequestHost = s"localhost:${logReceiverServerAddress.getPort.toString}"
    logReceiverClient = finagle.Http.newService(logReceiverRequestHost)
  }

  def performAdminRequest(request: Request): Future[Response] = {
    request.host = adminRequestHost
    adminClient(request)
  }

  def performLogReceiverRequest(request: Request): Future[Response] = {
    request.host = logReceiverRequestHost
    logReceiverClient(request)
  }

  def buildPostRequest(host: String, path: String, data: String): Request = {
    val url = s"http://$host$path"
    RequestBuilder().url(url).setHeader("Content-Type", "application/json").buildPost(utf8Buf(data))
  }
}
