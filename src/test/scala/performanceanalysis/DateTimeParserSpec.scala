package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class DateTimeParserSpec extends SpecBase {

  "DateParser" should {

    "Extract a date" in {
      DateTimeParser.mdy.parse("""[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Produce None for lines without date" in {
      DateTimeParser.mdy.parse("""[INFO] [] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Produce None for lines without valid date" in {
      DateTimeParser.mdy.parse("""[INFO] [02/30/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Extract a date with some flexibility" in {
      DateTimeParser.mdy.parse("""[INFO] [04/19/2016 14:17:16] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateTimeParser.mdy.parse("""[INFO] [04/19/2016 14:17:16.] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateTimeParser.mdy.parse("""[INFO] [04-19/2016 14:17:16.123456789] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.123456789"))
    }

    "Handle ymd format" in {
      DateTimeParser.ymd.parse("""......... [2016/04/19 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Handle dmy format" in {
      DateTimeParser.dmy.parse("""......... [19.4.2016 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Select correct format format" in {
      DateTimeParser.parser(Some("ymd")) shouldBe DateTimeParser.ymd
      DateTimeParser.parser(Some("dmy")) shouldBe DateTimeParser.dmy
      DateTimeParser.parser(Some("mdy")) shouldBe DateTimeParser.mdy
      DateTimeParser.parser(None) shouldBe DateTimeParser.mdy
    }

    "Throw an exception on non-existent format" in {
      intercept[IllegalArgumentException] {
        DateTimeParser.parser(Some("bla"))
      }
    }

  }

}
