package fi.proweb.train.helper

object DistanceCalculator {

  // Earth radius in km
  val earthRadius = 6371
  
  /**
   * Count distance of two coordinates
   */
  def countDistance(p1Lat: Double, p1Lon: Double, p2Lat: Double, p2Lon: Double): Int = {
    
    val dLat = (p1Lat - p2Lat).toRadians;    //delta (difference between) latitude in radians
    val dLon = (p1Lon - p2Lon).toRadians;    //delta (difference between) longitude in radians

    val p2LatR = p2Lat.toRadians;          //conversion to radians
    val p1LatR = p1Lat.toRadians;

    val a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(p2LatR) * Math.cos(p1LatR);
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));   //must use atan2 as simple arctan cannot differentiate 1/1 and -1/-1
    val distance = earthRadius * 1000 * c;   //sets the distance
    
    Math.round(distance).toInt
  }
  
  /**
   * Rectangle rounding circle with defined centerpoint coordinates and radius in meters
   * Returns (latMax, lonMax, latMin, lonMin)
   */
  def countRectangle(centerpoint: (Double, Double), radius: Int): ((Double, Double), (Double, Double)) = {   
    val latPlusData = findLatPlus(centerpoint, radius)
    val lonPlusData = findLonPlus(centerpoint, radius)
    val latMinusData = findLatMinus(centerpoint, radius)
    val lonMinusData = findLonMinus(centerpoint, radius)
      
    ((latPlusData._1._1 + latPlusData._2, lonPlusData._1._2 + lonPlusData._2), (latMinusData._1._1 - latMinusData._2, lonMinusData._1._2 - lonMinusData._2))
  }
  
  def findLatPlus = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1 + totIncr, centerpoint._2))_
  def findLatMinus = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1 - totIncr, centerpoint._2))_
  def findLonPlus = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1, centerpoint._2 + totIncr))_
  def findLonMinus = findPointAtDistance((centerpoint: (Double, Double), totIncr: Double) => countDistance(centerpoint._1, centerpoint._2, centerpoint._1, centerpoint._2 - totIncr))_
  
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