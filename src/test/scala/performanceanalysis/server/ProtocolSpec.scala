package performanceanalysis.server

import performanceanalysis.base.SpecBase
import com.typesafe.config.ConfigFactory
import performanceanalysis.server.Protocol.{LogParserCreated, LogParserExisted, LogParserNotFound, RegisterComponent}

/**
  * Created by janwillem on 25/05/16.
  */
class ProtocolSpec extends SpecBase {
  "Protocol" must {
    "correctly implements equals for RegisterComponent" in {
      val registerComponentA = RegisterComponent("Test!")
      val registerComponentB = RegisterComponent("Test!")
      registerComponentA.equals(registerComponentB) shouldBe (true)
    }

    "correctly implements equals for LogParserCreated" in {
      val logParserCreatedA = LogParserCreated("Created!")
      val logParserCreatedB = LogParserCreated("Kreated!")
      logParserCreatedA.equals(logParserCreatedB) shouldBe (false)
    }

    "correctly implements toString for LogParsedExisted" in {
      val logParserExisted = LogParserExisted("Hello!")
      logParserExisted.toString().equals("LogParserExisted(Hello!)") shouldBe (true)
    }

    "correctly implements toString for LogParserNotFound" in {
      val logParserNotFound = LogParserNotFound("Not found!")
      logParserNotFound.toString().equals("LogParserNotFound(Found!)") shouldBe (false)
    }


  }

}
