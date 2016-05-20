package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class DateParserSpec extends SpecBase {

  "DateParser" should {

    "Extract a date" in {
      DateParser.parseMDY("""[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Produce None for lines without date" in {
      DateParser.parseMDY("""[INFO] [] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Produce None for lines without valid date" in {
      DateParser.parseMDY("""[INFO] [02/30/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Extract a date with some flexibility" in {
      DateParser.parseMDY("""[INFO] [04/19/2016 14:17:16] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateParser.parseMDY("""[INFO] [04/19/2016 14:17:16.] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateParser.parseMDY("""[INFO] [04-19/2016 14:17:16.123456789] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.123456789"))
    }

    "Handle ymd format" in {
      DateParser.parseYMD("""......... [2016/04/19 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Handle dmy format" in {
      DateParser.parseDMY("""......... [19.4.2016 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

  }

}
