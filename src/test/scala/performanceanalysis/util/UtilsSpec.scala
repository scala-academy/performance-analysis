package performanceanalysis.util

import performanceanalysis.base.SpecBase
import performanceanalysis.util.Utils.split

/**
  * Created by seeta on 5/22/16.
  */
class UtilsSpec extends SpecBase {
  "The Utils split" must {

    "return array of lines if windows EOL exists" in {
      val input = "line1\r\nline2\r\nline3\r\n   line4\r\n   line5"
      val lines = split(input)
      lines.length shouldBe 5
      lines.mkString(",") shouldBe input.replace("\r\n", ",")
    }

    "return array of lines if unix or OSX EOL exists" in {
      val input = "line1\nline2\nline3\n   line4\n   line5"
      val lines = split(input)
      lines.length shouldBe 5
      lines.mkString(",") shouldBe input.replace("\n", ",")

      val values = split("some action took 200 ms\nsome action took 101 ms")
      values.length shouldBe 2
      values.mkString(",") shouldBe "some action took 200 ms,some action took 101 ms"
    }

    "return array of lines if mac EOL exists" in {
      val input = "line1\rline2\rline3\r   line4\r   line5"
      val lines = split(input)
      lines.length shouldBe 5
      lines.mkString(",") shouldBe input.replaceAll("\r", ",")
    }

    "return same input when no EOL" in {
      val input: String = "single line"
      split(input).mkString(",") shouldBe input
    }
  }
}
