package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class ParsedLineSpec extends SpecBase {

  "ParsedLine" must {


    "Extract a metric" in {
      val line = """[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("""Some action took (\d+) ms""")
      val result = parser.parse(line)
      result.dateTime shouldBe Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
      result.metric shouldBe Some("101")
    }

    "Supply None when no match is found" in {
      val line = """[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some other action took 101 ms"""
      val parser = LineParser("""Some action took (\d+) ms""")
      val result = parser.parse(line)
      result.metric shouldBe None
    }

  }

}
