package performanceanalysis.util

/**
  * Created by seeta on 5/23/16.
  */
object Utils {
  /**
    * This function returns an array of lines. It treats
    * \r\n, \n, \r as a new line
    * @param input to be split
    * @return array of lines
    */
  def splitIntoLines(input: String):Array[String] = {
    input.split("[\r\n]+")
  }
}
