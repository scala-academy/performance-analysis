/**
  * Ger van Rossum (c) 2016
  */
package performanceanalysis

import java.time.{LocalDate, LocalTime}

import scala.util.Try
import scala.util.matching.Regex.Match

class DateParser(iYear: Int, iMonth: Int, iDay: Int) {

  def parse(m: Match): LocalDate = {
    val year = m.group(iYear).toInt
    val month = m.group(iMonth).toInt
    val day = m.group(iDay).toInt
    LocalDate.of(year, month, day)
  }

}

class TimeParser(shift: Int) {

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
