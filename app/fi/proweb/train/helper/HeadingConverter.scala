package fi.proweb.train.helper

object HeadingConverter {

  def headingToString(heading: Int): String = {
    val directions: List[String] = List("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
    directions((Math.round((22 + heading.toDouble) % 360) / 45).toInt)
  }
  
}