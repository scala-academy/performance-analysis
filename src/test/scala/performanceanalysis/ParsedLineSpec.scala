package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class ParsedLineSpec extends SpecBase {

  "ParsedLine" must {

    "Extract a date" in {
      val line = """[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("")
      val result = parser.parse(line)
      result.dateTime.get shouldBe LocalDateTime.parse("2016-04-19T14:17:16.829")
    }

    "Produce None for lines without date" in {
      val line = """[INFO] [] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("")
      val result = parser.parse(line)
      result.dateTime shouldBe None
    }

    "Produce None for lines without invalid date" in {
      val line = """[INFO] [02/30/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("")
      val result = parser.parse(line)
      result.dateTime shouldBe None
    }

    "Extract a metric" in {
      val line = """[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("""Some action took (\d+) ms""")
      val result = parser.parse(line)
      result.metric.get shouldBe "101"
    }

    "Supply None when no match is found" in {
      val line = """[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some other action took 101 ms"""
      val parser = LineParser("""Some action took (\d+) ms""")
      val result = parser.parse(line)
      result.metric shouldBe None
    }

    "Extract a date with some flexibility 1" in {
      val line = """[INFO] [04/19/2016 14:17:16.] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("")
      val result = parser.parse(line)
      result.dateTime.get shouldBe LocalDateTime.parse("2016-04-19T14:17:16")
    }

    "Extract a date with some flexibility 2" in {
      val line = """[INFO] [04-19/2016 14:17:16.123456789] [Some.ClassName] Some action took 101 ms"""
      val parser = LineParser("")
      val result = parser.parse(line)
      result.dateTime.get shouldBe LocalDateTime.parse("2016-04-19T14:17:16.123456789")
    }

  }

}
