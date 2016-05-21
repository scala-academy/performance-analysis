package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class DateParserSpec extends SpecBase {

  "DateParser" should {

    "Extract a date" in {
      DateTimeParser.parseMDY("""[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Produce None for lines without date" in {
      DateTimeParser.parseMDY("""[INFO] [] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Produce None for lines without valid date" in {
      DateTimeParser.parseMDY("""[INFO] [02/30/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Extract a date with some flexibility" in {
      DateTimeParser.parseMDY("""[INFO] [04/19/2016 14:17:16] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateTimeParser.parseMDY("""[INFO] [04/19/2016 14:17:16.] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateTimeParser.parseMDY("""[INFO] [04-19/2016 14:17:16.123456789] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.123456789"))
    }

    "Handle ymd format" in {
      DateTimeParser.parseYMD("""......... [2016/04/19 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Handle dmy format" in {
      DateTimeParser.parseDMY("""......... [19.4.2016 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

  }

}
