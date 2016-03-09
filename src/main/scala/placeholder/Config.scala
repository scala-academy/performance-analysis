package placeholder

import com.typesafe.config.ConfigFactory

import scala.util.Try

trait Config {
  protected lazy val config = ConfigFactory.load()
  protected lazy val httpConfig = Try(config.getConfig("http")).getOrElse(ConfigFactory.empty())
  lazy val httpInterface = Try(httpConfig.getString("interface")).getOrElse("0.0.0.0")
  lazy val httpPort: Int = Try(httpConfig.getInt("port")).getOrElse(0)
}
