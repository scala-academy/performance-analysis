package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.DateTimeParser._

import scala.util.matching.Regex.Match
import scala.util.{Failure, Success, Try}

object DateTimeParser {

  private val regex = """(\d{1,2}|\d{4})[ -/\.](\d{1,2})[ -/\.](\d{1,2}|\d{4})[ T]( \d|\d{2})[:.](\d{2})[.:](\d{2})(\.\d{0,9})?""".r

  private def parser(iYear: Int, iMonth: Int, iDay: Int): DateTimeParser = new DateTimeParser(iYear, iMonth, iDay)

  val ymd = parser(1, 2, 3)

  val dmy = parser(3, 2, 1)

  val mdy = parser(3, 1, 2)

}

class DateTimeParser(iYear: Int, iMonth: Int, iDay: Int) {

  val rawDateParser = new DateParser(iYear, iMonth, iDay)

  val rawTimeParser = new TimeParser(3)

  def rawParser(m: Match): LocalDateTime = {
    LocalDateTime.of(rawDateParser.parse(m), rawTimeParser.parse(m))
  }

  def parse(s: String): Option[LocalDateTime] =
    Try(regex.findFirstMatchIn(s).map(rawParser)) match {
      case Success(option) => option
      case Failure(_) => None
    }

}
