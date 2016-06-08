package performanceanalysis

import java.time.LocalTime

import scala.util.Try
import scala.util.matching.Regex.Match

class TimeParser(shift: Int) {

  private val iHour = shift + 1

  private val iMin = shift + 2

  private val iSec = shift + 3

  private val iNano = iSec + 1

  private def nanoOption(s: String): Option[String] = {
    val nanoTry = Try(Option(s))
    nanoTry.toOption.flatten
  }

  def parse(m: Match): LocalTime = {
    val hour = m.group(iHour).toInt
    val min = m.group(iMin).toInt
    val sec = m.group(iSec).toInt
    val nano = nanoOption(m.group(iNano)) match {
      case None => 0
      case Some(".") => 0
      case Some(s) => (s.toDouble * 1000000000).toInt
    }
    LocalTime.of(hour, min, sec, nano)
  }

}
