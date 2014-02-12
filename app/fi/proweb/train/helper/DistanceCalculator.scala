package fi.proweb.train.helper

import java.math.BigDecimal

object DistanceCalculator {

  // Earth radius in km
  val earthRadius = 6371
  
  /**
   * Count distance of two coordinates
   */
  def countDistance(p1Lat: Double, p1Lon: Double, p2Lat: Double, p2Lon: Double): Int = {
    
    val dLat = (p1Lat - p2Lat).toRadians // Delta (difference between) latitude in radians
    val dLon = (p1Lon - p2Lon).toRadians // Delta (difference between) longitude in radians

    val p2LatR = p2Lat.toRadians // Conversion to radians
    val p1LatR = p1Lat.toRadians

    val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(p2LatR) * Math.cos(p1LatR)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)) // Must use atan2 as simple arctan cannot differentiate 1/1 and -1/-1
    val distance = earthRadius * 1000 * c // Sets the distance
    
    Math.round(distance).toInt
  }
  
  /**
   * Rectangle rounding circle with defined centerpoint coordinates and radius in meters
   * Returns ((latMax, lonMax), (latMin, lonMin))
   */
  def countRectangle(centerpoint: (Double, Double), radius: Int): ((Double, Double), (Double, Double)) = {   
    val latPlusData = findLatMax(centerpoint, radius)
    val lonPlusData = findLonMax(centerpoint, radius)
    val latMinusData = findLatMin(centerpoint, radius)
    val lonMinusData = findLonMin(centerpoint, radius)
      
    ((latPlusData._1._1 + latPlusData._2, lonPlusData._1._2 + lonPlusData._2), (latMinusData._1._1 - latMinusData._2, lonMinusData._1._2 - lonMinusData._2))
  }
  
  def findLatMax = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1 + totIncr, centerpoint._2))_
  def findLatMin = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1 - totIncr, centerpoint._2))_
  def findLonMax = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1, centerpoint._2 + totIncr))_
  def findLonMin = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1, centerpoint._2 - totIncr))_
  
  def findPointAtDistance(countDistance: (((Double, Double), Double) => Int))(centerpoint: (Double, Double), radius: Int): ((Double, Double), Double) = {
    var diff = 10
    val incr = 0.00001
    var totIncr = incr
    
    while (diff > 1) {
      diff = (radius - countDistance(centerpoint, totIncr)).abs
      totIncr += incr
    }
    
    (centerpoint, totIncr)
  }
}