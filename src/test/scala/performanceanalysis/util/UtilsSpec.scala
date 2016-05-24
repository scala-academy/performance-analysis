package performanceanalysis.util

import performanceanalysis.base.SpecBase
import performanceanalysis.util.Utils.splitIntoLines

/**
  * Created by seeta on 5/22/16.
  */
class UtilsSpec extends SpecBase {
  "The Utils split" must {
    val expected = Array("line1", "line2", "line3", "   line4", "\tline5")

    "return array of lines if windows EOL exists" in {
      val input = "line1\r\nline2\r\nline3\r\n   line4\r\n\tline5"
      splitIntoLines(input) shouldBe expected
    }

    "return array of lines if unix or OSX EOL exists" in {
      val input = "line1\nline2\nline3\n   line4\n\tline5"
      splitIntoLines(input) shouldBe expected
    }

    "return array of lines if mac EOL exists" in {
      val input = "line1\rline2\rline3\r   line4\r\tline5"
      splitIntoLines(input) shouldBe expected
    }

    "return same input when no EOL" in {
      val input: String = "single line"
      splitIntoLines(input) shouldBe Array(input)
    }
  }
}
