package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
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
  
  def nextStation: Option[TrainStation] = {  
    val revStations = stations.reverse
    if (revStations.exists(_.completed.getOrElse(false))) {
      val nextStationIndex = revStations.indexWhere(_.completed.getOrElse(false))
      Some(revStations(nextStationIndex - 1))
    } else {
      None
    }
  }
  
  override def toString = 
    "------------------------------" + Properties.lineSeparator +
    "Train:        " + title.getOrElse("?") + " (" + guid.getOrElse("?") + ")" + Properties.lineSeparator +
    "Distance:     " + distance.getOrElse("?") + " m" + Properties.lineSeparator +
    "Speed:        " + speed.getOrElse("?") + " km/h" + Properties.lineSeparator +
    "Heading:      " + heading.getOrElse("?") + Properties.lineSeparator +
    "Next station: " + nextStation.getOrElse("?") + Properties.lineSeparator +
    "------------------------------" + Properties.lineSeparator
}