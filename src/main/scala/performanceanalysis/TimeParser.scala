package performanceanalysis

import java.time.LocalTime

import scala.util.Try
import scala.util.matching.Regex.Match

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
