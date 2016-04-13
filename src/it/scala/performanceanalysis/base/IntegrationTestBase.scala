package performanceanalysis.base

import java.net.InetSocketAddress

import com.twitter.finagle
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, RequestBuilder, Response}
import com.twitter.io.Bufs._
import com.twitter.util.Future
import org.scalatest._
import performanceanalysis.administrator.Administrator
import performanceanalysis.logreceiver.LogReceiver
import performanceanalysis.server.Config

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.reflectiveCalls

trait IntegrationTestBase extends FeatureSpec with GivenWhenThen with Matchers with BeforeAndAfterAll {

  trait TestConfig extends Config {
    override lazy val adminHttpInterface = "localhost"
    override lazy val adminHttpPort: Int = 0
    override lazy val logReceiverHttpInterface = "localhost"
    override lazy val logReceiverHttpPort: Int = 0
  }

  val testMain = new App with TestConfig {
    val logReceiver = new LogReceiver with TestConfig
    val administrator = new Administrator(logReceiver.logReceiverActor) with TestConfig
  }

  lazy val adminServerAddress: InetSocketAddress = Await.result(testMain.administrator.getServerAddress, 10.seconds)
  lazy val adminRequestHost: String = s"localhost:${adminServerAddress.getPort.toString}"
  lazy val adminClient: Service[Request, Response] = finagle.Http.newService(adminRequestHost)

  lazy val logReceiverServerAddress: InetSocketAddress = Await.result(testMain.logReceiver.getServerAddress, 10.seconds)
  lazy val logReceiverRequestHost: String = s"localhost:${logReceiverServerAddress.getPort.toString}"
  lazy val logReceiverClient: Service[Request, Response] = finagle.Http.newService(logReceiverRequestHost)

  testMain.main(Array())

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
