package performanceanalysis

import java.time.{LocalDate, LocalDateTime, LocalTime}

import performanceanalysis.DateParser._

import scala.util.matching.Regex.Match
import scala.util.{Failure, Success, Try}

object DateParser {

  private val regex = """(\d{1,2}|\d{4})[ -/\.](\d{1,2})[ -/\.](\d{1,2}|\d{4})[ T]( \d|\d{2})[:.](\d{2})[.:](\d{2})(\.\d{0,9})?""".r

  private def parser(iYear: Int, iMonth: Int, iDay: Int): DateParser = new DateParser(iYear, iMonth, iDay)

  val parseYMD: (String) => Option[LocalDateTime] = parser(1, 2, 3).parser

  val parseDMY: (String) => Option[LocalDateTime] = parser(3, 2, 1).parser

  val parseMDY: (String) => Option[LocalDateTime] = parser(3, 1, 2).parser

}

private class DateParser(iYear: Int, iMonth: Int, iDay: Int) {

  def rawDateParser(m: Match): LocalDate = {
    val year = m.group(iYear).toInt
    val month = m.group(iMonth).toInt
    val day = m.group(iDay).toInt
    LocalDate.of(year, month, day)
  }

  def rawTimeParser(m: Match): LocalTime = {
    val hour = m.group(4).toInt
    val min = m.group(5).toInt
    val sec = m.group(6).toInt
    val nano = Try(Option(m.group(7))).toOption.flatten match {
      case None => 0
      case Some(".") => 0
      case Some(s) => (s.toDouble * 1000000000).toInt
    }
    LocalTime.of(hour, min, sec, nano)
  }

  def rawParser(m: Match): LocalDateTime = {
    LocalDateTime.of(rawDateParser(m), rawTimeParser(m))
  }

  def parser(s: String): Option[LocalDateTime] =
    Try(regex.findFirstMatchIn(s).map(rawParser)) match {
      case Success(option) => option
      case Failure(_) => None
    }

}
