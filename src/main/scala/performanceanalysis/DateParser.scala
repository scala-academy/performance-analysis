package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.DateParser._

import scala.util.matching.Regex.Match
import scala.util.{Failure, Success, Try}

object DateParser {

  private val regex = """(\d{1,2}|\d{4})[ -/\.](\d{1,2})[ -/\.](\d{1,2}|\d{4})[ T]( \d|\d{2})[:.](\d{2})[.:](\d{2})(\.\d{0,9})?""".r

  private def parseNano(s: String): Int = {
    // note that an optional group will be null when not supplied
    Try(Option(s)).toOption.flatten match {
      case None => 0
      case Some(".") => 0
      case Some(s) => (s.toDouble * 1000000000).toInt
    }
  }

  private def parser(iYear: Int, iMonth: Int, iDay: Int): DateParser = new DateParser(iYear, iMonth, iDay)


  val parseYMD: (String) => Option[LocalDateTime] = parser(1, 2, 3).parser

  val parseDMY: (String) => Option[LocalDateTime] = parser(3, 2, 1).parser

  val parseMDY: (String) => Option[LocalDateTime] = parser(3, 1, 2).parser


}

private class DateParser(iYear: Int, iMonth: Int, iDay: Int) {

  private def rawParser(m: Match): LocalDateTime = {
    val year = m.group(iYear).toInt
    val month = m.group(iMonth).toInt
    val day = m.group(iDay).toInt
    val hour = m.group(4).toInt
    val min = m.group(5).toInt
    val sec = m.group(6).toInt
    val nano = parseNano(m.group(7))
    LocalDateTime.of(year, month, day, hour, min, sec, nano)
  }

  def parser(s: String): Option[LocalDateTime] =
    Try(regex.findFirstMatchIn(s).map(rawParser)) match {
      case Success(option) => option
      case Failure(_) => None
    }

}
