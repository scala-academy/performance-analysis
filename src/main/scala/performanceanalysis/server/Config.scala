package performanceanalysis.server

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait Config {
  protected lazy val config = ConfigFactory.load()
  protected lazy val httpConfig = Try(config.getConfig("http")).getOrElse(ConfigFactory.empty())
  protected lazy val adminHttpConfig = Try(httpConfig.getConfig("admin")).getOrElse(ConfigFactory.empty())
  protected lazy val logReceiverHttpConfig = Try(httpConfig.getConfig("logReceiver")).getOrElse(ConfigFactory.empty())
  lazy val adminHttpInterface = Try(adminHttpConfig.getString("interface")).getOrElse("0.0.0.0")
  lazy val adminHttpPort: Int = Try(adminHttpConfig.getInt("port")).getOrElse(0)
  lazy val logReceiverHttpInterface = Try(logReceiverHttpConfig.getString("interface")).getOrElse("0.0.0.0")
  lazy val logReceiverHttpPort: Int = Try(logReceiverHttpConfig.getInt("port")).getOrElse(0)
}
