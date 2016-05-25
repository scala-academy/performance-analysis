package performanceanalysis.server

import performanceanalysis.base.SpecBase
import com.typesafe.config.ConfigFactory
import performanceanalysis.server.Protocol.{LogParserCreated, LogParserExisted, LogParserNotFound, RegisterComponent}

/**
  * Created by janwillem on 25/05/16.
  */
class ProtocolSpec extends SpecBase {
  "Protocol" must {
    "correctly implement equals for RegisterComponent" in {
      val registerComponentA = RegisterComponent("Test!")
      val registerComponentB = RegisterComponent("Test!")
      registerComponentA.equals(registerComponentB) shouldBe (true)
    }

    "correctly implement equals for LogParserCreated" in {
      val logParserCreatedA = LogParserCreated("Created!")
      val logParserCreatedB = LogParserCreated("Kreated!")
      logParserCreatedA.equals(logParserCreatedB) shouldBe (false)
    }

    "correctly implement toString for LogParsedExisted" in {
      val logParserExisted = LogParserExisted("Hello!")
      logParserExisted.toString().equals("LogParserExisted(Hello!)") shouldBe (true)
    }

    "correctly implement toString for LogParserNotFound" in {
      val logParserNotFound = LogParserNotFound("Not found!")
      logParserNotFound.toString().equals("LogParserNotFound(Found!)") shouldBe (false)
    }


  }

}
