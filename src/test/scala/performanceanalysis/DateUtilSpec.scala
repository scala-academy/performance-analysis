package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.base.SpecBase

class DateUtilSpec extends SpecBase {

  "DateUtil" should {

    "Extract a date" in {
      DateUtil.mdyParser("""[INFO] [04/19/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Produce None for lines without date" in {
      DateUtil.mdyParser("""[INFO] [] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Produce None for lines without valid date" in {
      DateUtil.mdyParser("""[INFO] [02/30/2016 14:17:16.829] [Some.ClassName] Some action took 101 ms""") shouldBe None
    }

    "Extract a date with some flexibility" in {
      DateUtil.mdyParser("""[INFO] [04/19/2016 14:17:16] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateUtil.mdyParser("""[INFO] [04/19/2016 14:17:16.] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16"))

      DateUtil.mdyParser("""[INFO] [04-19/2016 14:17:16.123456789] [Some.ClassName] Some action took 101 ms""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.123456789"))
    }

    "Handle ymd format" in {
      DateUtil.ymdParser("""......... [2016/04/19 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

    "Handle dmy format" in {
      DateUtil.dmyParser("""......... [19.4.2016 14:17:16.829] .............""") shouldBe
        Some(LocalDateTime.parse("2016-04-19T14:17:16.829"))
    }

  }

}
