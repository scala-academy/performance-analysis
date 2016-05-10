package performanceanalysis

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException, ResolverStyle}

import scala.util.matching.Regex

object LineParser {

  def apply(regex: Regex): LineParser = new LineParser(regex)

  def apply(string: String): LineParser = apply(string.r)

}

class LineParser(regex: Regex) {

  def parse(line: String): ParsedLine = new ParsedLine(line, regex)

}

class ParsedLine(line: String, regex: Regex) {

  private val dateFormat = DateTimeFormatter.
    ofPattern("MM/dd/uuuu HH:mm:ss.SSS").
    withResolverStyle(ResolverStyle.STRICT)

  lazy val dateTime: Option[LocalDateTime] = {
    val dateString: Option[String] = """(\d+/\d+/\d+ \d+:\d+:\d+.\d+)""".r.findFirstIn(line)
    dateString.flatMap({ s =>
      try {
        Some(LocalDateTime.parse(s, dateFormat))
      } catch {
        case e: DateTimeParseException => None
      }
    })
  }

  lazy val metric: Option[String] = regex.findFirstMatchIn(line).filter(_.groupCount >= 1).map(_ group 1)

}
