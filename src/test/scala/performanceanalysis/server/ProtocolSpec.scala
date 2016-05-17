package performanceanalysis.server

import performanceanalysis.base.SpecBase
import performanceanalysis.server.Protocol.{Metric, ValueType}
import spray.json._

import scala.concurrent.duration.Duration

class ProtocolSpec extends SpecBase {

  "Protocol" must {
    "serialize/deserialize Metric with value type 'boolean'" in new Protocol {
      val json = """{"metric-key":"mk","regex":"rx","value-type":"boolean"}"""
      val metric = json.parseJson.convertTo[Metric]
      metric.valueType shouldBe ValueType(classOf[Boolean])
      metric.toJson.toString shouldBe json
    }

    "serialize/deserialize Metric with value type 'string'" in new Protocol {
      val json = """{"metric-key":"mk","regex":"rx","value-type":"string"}"""
      val metric = json.parseJson.convertTo[Metric]
      metric.valueType shouldBe ValueType(classOf[String])
      metric.toJson.toString shouldBe json
    }

    "serialize/deserialize Metric with value type 'duration'" in new Protocol {
      val json = """{"metric-key":"mk","regex":"rx","value-type":"duration"}"""
      val metric = json.parseJson.convertTo[Metric]
      metric.valueType shouldBe ValueType(classOf[Duration])
    }

    "fail to deserialize to Metric when unknown value type" in new Protocol {
      val json = """{"metric-key":"mk","regex":"rx","value-type":"BrandNewType"}"""
      intercept[DeserializationException] {
        json.parseJson.convertTo[Metric]
      }
    }

    "fail to serialize to json when unknown value type" in new Protocol {
      class BrandNewType {}
      val metric = Metric("mk", "rx", ValueType(classOf[BrandNewType]))
      intercept[SerializationException] {
        metric.toJson
      }
    }
  }
}
