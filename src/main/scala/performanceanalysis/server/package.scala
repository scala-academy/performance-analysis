package performanceanalysis

import performanceanalysis.server.Protocol.ValueType

import scala.concurrent.duration.Duration

package object server {

  implicit class StringConversions(val value: String) {
    def toType(valueType: ValueType): Any = valueType.aType match {
      case x if x == classOf[Boolean] => !"".equals(value.trim)
      case x if x == classOf[Duration] => Duration(value)
      case _ => value
    }
  }
}
