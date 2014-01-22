package fi.proweb.train.helper

object DistanceCalculator {

  // Earth radius in km
  val earthRadius = 6371
  
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
  
}