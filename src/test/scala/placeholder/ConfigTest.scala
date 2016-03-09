package placeholder

import com.typesafe.config.ConfigFactory
import placeholder.base.SpecBase

/**
 * Created by Jordi on 9-3-2016.
 */
class ConfigTest extends SpecBase {
  "Config" must {
    "read an int value for a normal config.httpConfig.httpPort" in new Config {
      override lazy val config = ConfigFactory.load("normal.conf")
      httpPort shouldBe 8080
    }
    "return 0 for config.httpConfig.httpPort in case of non existing httpPort config" in new Config {
      override lazy val config = ConfigFactory.load("noHttpConf.conf")
      httpPort shouldBe 0
    }
    "return 0 for config.httpConfig.httpPort in case of  httpPort config" in new Config {
      override lazy val config = ConfigFactory.load("nonnumericHttpPort.conf")
      httpPort shouldBe 0
    }
    "read a string for a normal config.httpConfig.interface" in new Config {
      override lazy val config = ConfigFactory.load("normal.conf")
      httpInterface shouldBe "127.0.0.1"
    }
    """return "0.0.0.0" for config.httpConfig.httpPort in case of non existing httpPort config""" in new Config {
      override lazy val config = ConfigFactory.load("noHttpConf.conf")
      httpInterface shouldBe "0.0.0.0"
    }
  }
}
