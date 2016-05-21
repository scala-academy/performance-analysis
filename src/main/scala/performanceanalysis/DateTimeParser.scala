package performanceanalysis

import java.time.{LocalDate, LocalDateTime, LocalTime}

import performanceanalysis.DateTimeParser._

import scala.util.matching.Regex.Match
import scala.util.{Failure, Success, Try}

object DateTimeParser {

  private val regex = """(\d{1,2}|\d{4})[ -/\.](\d{1,2})[ -/\.](\d{1,2}|\d{4})[ T]( \d|\d{2})[:.](\d{2})[.:](\d{2})(\.\d{0,9})?""".r

  private def parser(iYear: Int, iMonth: Int, iDay: Int): DateTimeParser = new DateTimeParser(iYear, iMonth, iDay)

  val parseYMD: (String) => Option[LocalDateTime] = parser(1, 2, 3).parser

  val parseDMY: (String) => Option[LocalDateTime] = parser(3, 2, 1).parser

  val parseMDY: (String) => Option[LocalDateTime] = parser(3, 1, 2).parser

}

private class DateParser(iYear: Int, iMonth: Int, iDay: Int) {

  def parse(m: Match): LocalDate = {
    val year = m.group(iYear).toInt
    val month = m.group(iMonth).toInt
    val day = m.group(iDay).toInt
    LocalDate.of(year, month, day)
  }

}

private class TimeParser(shift: Int) {

  val iHour = shift + 1

  val iMin = shift + 2

  val iSec = shift + 3

  val iNano = iSec + 1

  def parse(m: Match): LocalTime = {
    val hour = m.group(iHour).toInt
    val min = m.group(iMin).toInt
    val sec = m.group(iSec).toInt
    val nano = Try(Option(m.group(iNano))).toOption.flatten match {
      case None => 0
      case Some(".") => 0
      case Some(s) => (s.toDouble * 1000000000).toInt
    }
    LocalTime.of(hour, min, sec, nano)
  }

}

private class DateTimeParser(iYear: Int, iMonth: Int, iDay: Int) {

  val rawDateParser = new DateParser(iYear, iMonth, iDay)

  val rawTimeParser = new TimeParser(3)

  def rawParser(m: Match): LocalDateTime = {
    LocalDateTime.of(rawDateParser.parse(m), rawTimeParser.parse(m))
  }

  def parser(s: String): Option[LocalDateTime] =
    Try(regex.findFirstMatchIn(s).map(rawParser)) match {
      case Success(option) => option
      case Failure(_) => None
    }

}
