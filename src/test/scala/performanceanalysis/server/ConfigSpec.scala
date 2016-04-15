package performanceanalysis.server

import com.typesafe.config.ConfigFactory
import performanceanalysis.base.SpecBase

/**
  * Created by Jordi on 9-3-2016.
  */
class ConfigSpec extends SpecBase {
  "Config" must {
    "read an int value for a normal config.httpConfig.admin.httpPort" in new Config {
      override lazy val config = ConfigFactory.load("normal.conf")
      adminHttpPort shouldBe 8080
    }
    "return 0 for config.httpConfig.admin.httpPort in case of non existing httpPort config" in new Config {
      override lazy val config = ConfigFactory.load("noHttpConf.conf")
      adminHttpPort shouldBe 0
    }
    "return 0 for config.httpConfig.admin.httpPort in case of httpPort config" in new Config {
      override lazy val config = ConfigFactory.load("nonnumericHttpPort.conf")
      adminHttpPort shouldBe 0
    }
    "read a string for a normal config.httpConfig.admin.interface" in new Config {
      override lazy val config = ConfigFactory.load("normal.conf")
      adminHttpInterface shouldBe "127.0.0.1"
    }
    """return "0.0.0.0" for config.httpConfig.admin.httpPort in case of non existing httpPort config""" in new Config {
      override lazy val config = ConfigFactory.load("noHttpConf.conf")
      adminHttpInterface shouldBe "0.0.0.0"
    }
  }
  "Config" must {
    "read an int value for a normal config.httpConfig.logReceiver.httpPort" in new Config {
      override lazy val config = ConfigFactory.load("normal.conf")
      logReceiverHttpPort shouldBe 8088
    }
    "return 0 for config.httpConfig.logReceiver.httpPort in case of non existing httpPort config" in new Config {
      override lazy val config = ConfigFactory.load("noHttpConf.conf")
      logReceiverHttpPort shouldBe 0
    }
    "return 0 for config.httpConfig.logReceiver.httpPort in case of  httpPort config" in new Config {
      override lazy val config = ConfigFactory.load("nonnumericHttpPort.conf")
      logReceiverHttpPort shouldBe 0
    }
    "read a string for a normal config.httpConfig.logReceiver.interface" in new Config {
      override lazy val config = ConfigFactory.load("normal.conf")
      logReceiverHttpInterface shouldBe "127.0.0.1"
    }
    """return "0.0.0.0" for config.httpConfig.logReceiver.httpPort in case of non existing httpPort config""" in new Config {
      override lazy val config = ConfigFactory.load("noHttpConf.conf")
      logReceiverHttpInterface shouldBe "0.0.0.0"
    }
  }
}
