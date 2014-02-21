package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
import fi.proweb.train.data.RailNetwork
import util.Properties
import scala.collection.mutable.MutableList
import scala.collection.mutable.Queue

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
  
  var history: Queue[Train] = Queue[Train]()
  
  var jammed = false
    
  def makeCopy: Train = {
    val train = new Train
    train.guid = guid
    train.title = title
    train.lastBuildDate = lastBuildDate
    train.distance = distance
    train.location = location
    train.speed = speed
    train.heading = heading
    
    train.stations = MutableList[TrainStation]()
    stations.foreach(train.stations += _.makeCopy)
    
    train.history = history
    train.jammed = jammed
    
    train
  }
  
  def distanceInKm: Option[Double] = {
    distance.map((dist: Int) => (dist.toDouble / 100d).round.toDouble / 10d)
  }
  
  def nextStation: TrainStation = {  
    val revStations = stations.reverse
    if (revStations.exists(_.completed.getOrElse(false))) {
      val nextStationIndex = revStations.indexWhere(_.completed.getOrElse(false))
      revStations(nextStationIndex - 1)
    } else {
      stations.head
    }
  }
  
  def nextStationCode: String = {
    nextStation.stationCode.getOrElse("?")
  }
  
  def stationTitle(stationCode: String): String = {
    if (RailNetwork.stations.isDefinedAt(stationCode)) {
      RailNetwork.stations(stationCode).title.getOrElse(stationCode)
    } else {
      stationCode
    }
  }
  
  def hasLocation: Boolean = {
    val locZero = ((location != None) && (location.get._1 < 0.001) && (location.get._2 < 0.001))
    val locNone = (location == None)
    !(locZero || locNone)
  }
  
  override def toString = 
    "------------------------------" + Properties.lineSeparator +
    "Train:        " + getTitle + " (" + getGuid + ")" + Properties.lineSeparator +
    "Distance:     " + getDistanceInKm + " km" + Properties.lineSeparator +
    "Speed:        " + getSpeed + " km/h" + Properties.lineSeparator +
    "Heading:      " + getHeading + Properties.lineSeparator +
    "Next station: " + getNextStation + Properties.lineSeparator +
    "------------------------------" + Properties.lineSeparator
    
  private def getTitle: String = {
    title.getOrElse("?")
  }
    
  private def getGuid: String = {
    guid.getOrElse("?")
  }
  
  private def getDistanceInKm: String = {
    if (jammed) {
      "JAMMED"
    } else {
      distanceInKm.getOrElse("?").toString
    }
  }
  
  private def getSpeed: String = {
    if (jammed) {
      "JAMMED"
    } else {
      speed.getOrElse("?").toString
    }
  }
  
  private def getHeading: String = {
    if (jammed) {
      "JAMMED"
    } else {
      heading.getOrElse("?")
    }
  }
  
  private def getNextStation: String = {
    if (jammed) {
      "JAMMED"
    } else {
      stationTitle(nextStationCode)
    }
  }
}