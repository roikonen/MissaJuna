package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
import fi.proweb.train.data.RailNetwork
import util.Properties
import scala.collection.mutable.MutableList

class Train extends AppData[Train] {

  // Train ID
  var guid: Option[String] = None
  
  // Train title
  var title: Option[String] = None
  
  // Location update time
  var lastBuildDate: Option[String] = None
  
  // Distance in m
  var distance: Option[Int] = None
  
  // Geo location (georss)
  var location: Option[(Double, Double)] = None

  // Speed in km/h
  var speed: Option[Int] = None
  
  // Heading: N, NE, E, SE, S, SW, W
  var heading: Option[String] = None
  
  var stations: MutableList[TrainStation] = MutableList[TrainStation]()
  
  def distanceInKm: Option[Int] = {
    distance.map((dist: Int) => (dist.toDouble / 1000d).round.toInt)
  }
  
  def nextStation: Option[TrainStation] = {  
    val revStations = stations.reverse
    if (revStations.exists(_.completed.getOrElse(false))) {
      val nextStationIndex = revStations.indexWhere(_.completed.getOrElse(false))
      Some(revStations(nextStationIndex - 1))
    } else {
      None
    }
  }
  
  def nextStationCode: String = {
    if (nextStation != None) nextStation.get.stationCode.getOrElse("?")
    else "?"
  }
  
  def stationTitle(stationCode: String): String = {
    if (RailNetwork.stations.isDefinedAt(stationCode)) {
      RailNetwork.stations(stationCode).title.getOrElse(stationCode)
    } else {
      stationCode
    }
  }
  
  override def toString = 
    "------------------------------" + Properties.lineSeparator +
    "Train:        " + title.getOrElse("?") + " (" + guid.getOrElse("?") + ")" + Properties.lineSeparator +
    "Distance:     " + distanceInKm.getOrElse("?") + " km" + Properties.lineSeparator +
    "Speed:        " + speed.getOrElse("?") + " km/h" + Properties.lineSeparator +
    "Heading:      " + heading.getOrElse("?") + Properties.lineSeparator +
    "Next station: " + stationTitle(nextStationCode) + Properties.lineSeparator +
    "------------------------------" + Properties.lineSeparator
}