package periphas.server

import org.scalatest.concurrent.ScalaFutures
import periphas.base.SpecBase
import scala.concurrent.duration._

class MainSpec extends SpecBase with ScalaFutures {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Main.main(Array())
  }
  val main = Main

  "Main" must {
    "start an instance of LogReceiver" in {
      main.logReceiver.getServerAddress.isReadyWithin(1.second) should be (true)
    }
    "start an instance of Administrator" in {
      main.administrator.getServerAddress.isReadyWithin(1.second) should be (true)
    }
  }

}
