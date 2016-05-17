package performanceanalysis

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder, DateTimeParseException, ResolverStyle}
import java.time.temporal.ChronoField

import scala.util.matching.Regex

object LineParser {

  def apply(regex: Regex): LineParser = new LineParser(regex)

  def apply(string: String): LineParser = apply(string.r)

}

class LineParser(regex: Regex) {

  def parse(line: String): ParsedLine = new ParsedLine(line, regex)

}

object ParsedLine {

  val dtRegex = """\d{1,2}[ -/.]\d{1,2}[ -/.]\d{4}[ T]\d{1,2}[:.]\d{2}[.:]\d{2}(.\d{0,9})?""".r

  private def formatWithFraction(pattern: String): DateTimeFormatter = new DateTimeFormatterBuilder()
    .appendPattern(pattern)
    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
    .toFormatter
    .withResolverStyle(ResolverStyle.STRICT)

  private val ymdFormat = formatWithFraction("uuuu[/][-][.][ ]m[/][-][.][ ]d HH[:][.]mm[:][.]ss")

  private val dmyFormat = formatWithFraction("d[/][-][.][ ]M[/][-][.][ ]uuuu HH[:][.]mm[:][.]ss")

  private val mdyFormat = formatWithFraction("M[/][-][.][ ]d[/][-][.][ ]uuuu HH[:][.]mm[:][.]ss")

}

class ParsedLine(line: String, regex: Regex) {

  import ParsedLine._

  lazy val dateTime: Option[LocalDateTime] = {
    val dateString: Option[String] = dtRegex.findFirstIn(line)
    dateString.flatMap({ s =>
      try {
        Some(LocalDateTime.parse(s, mdyFormat))
      } catch {
        case e: DateTimeParseException => None
      }
    })
  }

  lazy val metric: Option[String] = regex.findFirstMatchIn(line).filter(_.groupCount >= 1).map(_ group 1)

}
