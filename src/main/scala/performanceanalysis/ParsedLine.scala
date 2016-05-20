package performanceanalysis

import java.time.LocalDateTime

import performanceanalysis.DateUtil._

import scala.util.matching.Regex

object LineParser {

  def apply(regex: Regex): LineParser = new LineParser(regex)

  def apply(string: String): LineParser = apply(string.r)

}

class LineParser(regex: Regex) {

  def parse(line: String): ParsedLine = new ParsedLine(line, regex)

}

object ParsedLine {


}

class ParsedLine(line: String, regex: Regex) {

  lazy val dateTime: Option[LocalDateTime] = mdyParser(line)

  lazy val metric: Option[String] = regex.findFirstMatchIn(line).filter(_.groupCount >= 1).map(_ group 1)

}
