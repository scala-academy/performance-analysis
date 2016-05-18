package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class ParsedLineSpec extends SpecBase {

  implicit class PatternTester(string: String) {

    def shouldParseToDateString(dateString: String): Unit = {
      val parser = LineParser("")
      val result = parser.parse(string)
      result.dateTime shouldBe Some(LocalDateTime.parse(dateString))
    }

    def shouldParseToNone(): Unit = {
      val parser = LineParser("")
      val result = parser.parse(string)
      result.dateTime shouldBe None
    }

  }

  "ParsedLine" must {

    "Extract a date" in {
      """[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""" shouldParseToDateString
        "2016-04-19T14:17:16.829"
    }

    "Produce None for lines without date" in {
      """[INFO] [] [Some.ClassName] Some action took 101 ms""" shouldParseToNone()
    }

    "Produce None for lines without valid date" in {
      """[INFO] [02/30/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""" shouldParseToNone()
    }

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

    "Extract a date with some flexibility 1" in {
      """[INFO] [04/19/2016 14:17:16] [Some.ClassName] Some action took 101 ms""" shouldParseToDateString
        "2016-04-19T14:17:16"
    }

    "Extract a date with some flexibility 2" in {
      """[INFO] [04/19/2016 14:17:16.] [Some.ClassName] Some action took 101 ms""" shouldParseToDateString
        "2016-04-19T14:17:16"
    }

    "Extract a date with some flexibility 3" in {
      """[INFO] [04-19/2016 14:17:16.123456789] [Some.ClassName] Some action took 101 ms""" shouldParseToDateString
        "2016-04-19T14:17:16.123456789"
    }

  }

}
