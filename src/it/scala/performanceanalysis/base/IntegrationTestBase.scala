package performanceanalysis.base

import java.net.InetSocketAddress

import com.twitter.finagle
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, RequestBuilder, Response}
import com.twitter.io.Bufs._
import com.twitter.util.Future
import com.typesafe.config.ConfigFactory
import org.scalatest._
import performanceanalysis.administrator.Administrator
import performanceanalysis.logreceiver.LogReceiver
import performanceanalysis.server.{Config, Main}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

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

  var adminServerAddress: InetSocketAddress = _
  var adminRequestHost: String = _
  var adminClient: Service[Request, Response] = _

  var logReceiverServerAddress: InetSocketAddress = _
  var logReceiverRequestHost: String = _
  var logReceiverClient: Service[Request, Response] = _

  testMain.main(Array())

  override def beforeAll(): Unit = {
    adminServerAddress = Await.result(testMain.administrator.getServerAddress, 10.seconds)
    adminRequestHost = s"localhost:${adminServerAddress.getPort.toString}"
    adminClient = finagle.Http.newService(adminRequestHost)

    logReceiverServerAddress = Await.result(testMain.logReceiver.getServerAddress, 10.seconds)
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
