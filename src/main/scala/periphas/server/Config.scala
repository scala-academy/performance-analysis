package periphas.server

import com.typesafe.config.{ConfigFactory, Config => TypesafeConfig}

import scala.util.Try

trait Config {
  protected lazy val config: TypesafeConfig = ConfigFactory.load()
  protected lazy val httpConfig: TypesafeConfig = Try(config.getConfig("http")).getOrElse(ConfigFactory.empty())
  protected lazy val adminHttpConfig: TypesafeConfig = Try(httpConfig.getConfig("admin")).getOrElse(ConfigFactory.empty())
  protected lazy val logReceiverHttpConfig: TypesafeConfig = Try(httpConfig.getConfig("logReceiver")).getOrElse(ConfigFactory.empty())
  lazy val adminHttpInterface: String = Try(adminHttpConfig.getString("interface")).getOrElse("0.0.0.0")
  lazy val adminHttpPort: Int = Try(adminHttpConfig.getInt("port")).getOrElse(0)
  lazy val logReceiverHttpInterface: String = Try(logReceiverHttpConfig.getString("interface")).getOrElse("0.0.0.0")
  lazy val logReceiverHttpPort: Int = Try(logReceiverHttpConfig.getInt("port")).getOrElse(0)
}
