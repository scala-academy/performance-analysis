package performanceanalysis.server

import performanceanalysis.base.SpecBase
import performanceanalysis.server.Protocol.ValueType

import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps

class StringConversionsSpec extends SpecBase {

  "StringConversions" must {
      "convert string to boolean" in {
        new StringConversions("ERROR").toType(ValueType(classOf[Boolean])) shouldBe true
      }
      "convert string to duration" in {
        new StringConversions("100 ms").toType(ValueType(classOf[Duration])) shouldBe (100 millis)
      }
      "no conversion when string to new type" in {
        class MyNewType
        new StringConversions("value not handled").toType(ValueType(classOf[MyNewType])) shouldBe "value not handled"
      }
    }
}
