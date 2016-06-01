package performanceanalysis

import java.time.LocalDate

import scala.util.matching.Regex.Match

class DateParser(iYear: Int, iMonth: Int, iDay: Int) {

  def parse(m: Match): LocalDate = {
    val year = m.group(iYear).toInt
    val month = m.group(iMonth).toInt
    val day = m.group(iDay).toInt
    LocalDate.of(year, month, day)
  }

}
