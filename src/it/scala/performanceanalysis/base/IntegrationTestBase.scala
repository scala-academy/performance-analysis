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
import performanceanalysis.utils.TwitterFutures

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.reflectiveCalls

trait IntegrationTestBase
  extends FeatureSpec
  with GivenWhenThen
  with Matchers
  with BeforeAndAfterAll
  with TwitterFutures {

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

  testMain.main(Array())

  val adminServerAddress: InetSocketAddress = Await.result(testMain.administrator.getServerAddress, 10.seconds)
  val adminRequestHost: String = s"localhost:${adminServerAddress.getPort.toString}"
  val adminClient: Service[Request, Response] = finagle.Http.newService(adminRequestHost)

  val logReceiverServerAddress: InetSocketAddress = Await.result(testMain.logReceiver.getServerAddress, 10.seconds)
  val logReceiverRequestHost: String = s"localhost:${logReceiverServerAddress.getPort.toString}"
  val logReceiverClient: Service[Request, Response] = finagle.Http.newService(logReceiverRequestHost)

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

  def buildGetRequest(host: String, path: String): Request = {
    val url = s"http://$host$path"
    RequestBuilder().url(url).buildGet()
  }

  def awaitResponse(server: Service[Request, Response], request: Request): Response = {
    val responseFuture = server(request)
    responseFuture.futureValue
  }

  def adminGetResonse(path: String): Future[Response] = {
    val request = buildGetRequest(adminRequestHost, path)
    adminClient(request)
  }

  def awaitAdminPostResonse(path: String, data: String): Response = {
    val request = buildPostRequest(adminRequestHost, path, data)
    awaitResponse(adminClient, request)
  }

  def awaitAdminGetResonse(path: String): Response = {
    val request = buildGetRequest(adminRequestHost, path)
    awaitResponse(adminClient, request)
  }
}
