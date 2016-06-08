package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.RegexDateTimeParser._

import scala.util.Try
import scala.util.matching.Regex.Match

object DateTimeParser {

  private def regexParser(iYear: Int, iMonth: Int, iDay: Int) = new RegexDateTimeParser(iYear, iMonth, iDay)

  val ymd = regexParser(1, 2, 3)

  val dmy = regexParser(3, 2, 1)

  val mdy = regexParser(3, 1, 2)

  def parser(fmt: Option[String]): DateTimeParser = fmt match {
    case None => mdy
    case Some(s) => s.toLowerCase match {
      case "ymd" => ymd
      case "dmy" => dmy
      case "mdy" => mdy
      case _ => throw new IllegalArgumentException
    }
  }

}

trait DateTimeParser {

  def parse(s: String): Option[LocalDateTime]

}

object RegexDateTimeParser {

  private val regex = """(\d{1,2}|\d{4})[ -/\.](\d{1,2})[ -/\.](\d{1,2}|\d{4})[ T]( \d|\d{2})[:.](\d{2})[.:](\d{2})(\.\d{0,9})?""".r

}

class RegexDateTimeParser(iYear: Int, iMonth: Int, iDay: Int) extends DateTimeParser {

  val rawDateParser = new DateParser(iYear, iMonth, iDay)

  val rawTimeParser = new TimeParser(3)

  def rawParser(m: Match): LocalDateTime = {
    LocalDateTime.of(rawDateParser.parse(m), rawTimeParser.parse(m))
  }

  def parse(s: String): Option[LocalDateTime] =
    Try(regex.findFirstMatchIn(s).map(rawParser)).toOption.flatten

}
