package performanceanalysis

import java.time.{DateTimeException, LocalDateTime}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, ResolverStyle}
import java.time.temporal.ChronoField

import scala.util.Try
import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object LineParser {

  def apply(regex: Regex): LineParser = new LineParser(regex)

  def apply(string: String): LineParser = apply(string.r)

}

class LineParser(regex: Regex) {

  def parse(line: String): ParsedLine = new ParsedLine(line, regex)

}

object ParsedLine {

  val dtRegex = """(\d{1,2})[ -/.](\d{1,2})[ -/.](\d{4})[ T]( \d|\d{2})[:.](\d{2})[.:](\d{2})(\.\d{0,9})?""".r

  def dateParser(iYear: Int, iMonth: Int, iDay: Int): String => Option[LocalDateTime] = {
    (s: String) => try {
      val dtr: Option[Match] = dtRegex.findFirstMatchIn(s)
      dtr.map { m =>
        val year = m.group(iYear).toInt
        val month = m.group(iMonth).toInt
        val day = m.group(iDay).toInt
        val hour = m.group(4).toInt
        val min = m.group(5).toInt
        val sec = m.group(6).toInt
        val nanoOption: Option[String] = Try(Option(m.group(7))).toOption.flatten
        val nano = nanoOption match {
          case None => 0
          case Some(".") => 0
          case Some(s) => (s.toDouble * 1000000000).toInt
        }
        LocalDateTime.of(year, month, day, hour, min, sec, nano)
      }
    } catch {
      case e: MatchError => None
      case e: DateTimeException => None
    }
  }

  private val ymdParser = dateParser(1, 2, 3)

  private val dmyParser = dateParser(3, 2, 1)

  private val mdyParser = dateParser(3, 1, 2)

}

class ParsedLine(line: String, regex: Regex) {

  import ParsedLine._

  lazy val dateTime: Option[LocalDateTime] = mdyParser(line)

  lazy val metric: Option[String] = regex.findFirstMatchIn(line).filter(_.groupCount >= 1).map(_ group 1)

}
