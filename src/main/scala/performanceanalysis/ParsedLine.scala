package performanceanalysis

import java.time.LocalDateTime

import scala.util.matching.Regex

object LineParser {

  def apply(regex: Regex): LineParser = new LineParser(regex, DateTimeParser.mdy)

  def apply(string: String): LineParser = apply(string.r)

}

class LineParser(regex: Regex, dateTimeParser: DateTimeParser) {

  def parse(line: String): ParsedLine = new ParsedLine(line, regex, dateTimeParser)

}

class ParsedLine(line: String, regex: Regex, dateTimeParser: DateTimeParser) {

  lazy val dateTime: Option[LocalDateTime] = dateTimeParser.parse(line)

  lazy val metric: Option[String] = regex.findFirstMatchIn(line).filter(_.groupCount >= 1).map(_ group 1)

}
