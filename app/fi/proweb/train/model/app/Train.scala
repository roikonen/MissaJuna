package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
import fi.proweb.train.data.RailNetwork
import util.Properties
import scala.collection.mutable.MutableList
import scala.collection.mutable.Queue
import fi.proweb.train.helper.TrainDistanceCalculator

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
  var jammedRatio = 0d
  var jammedRatioSampleSize = 0
    
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
    train.jammedRatio = jammedRatio
    train.jammedRatioSampleSize = jammedRatioSampleSize
        
    train
  }
  
  def distanceInKm: Option[Double] = {
    distance.map((dist: Int) => (dist.toDouble / 100d).round.toDouble / 10d)
  }
  
  def lastStation: TrainStation = {  
    stations.last
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
  
  def previousStation: TrainStation = {  
    val revStations = stations.reverse
    if (revStations.exists(_.completed.getOrElse(false))) {
      val nextStationIndex = revStations.indexWhere(_.completed.getOrElse(false))
      revStations(nextStationIndex)
    } else {
      stations.head
    }
  }
  
  def firstStation: TrainStation = {  
    stations.head
  }
  
  def ripStationCode(station: TrainStation): String =  {
    station.stationCode.getOrElse("?")
  }
  
  
  def stationTitle(stationCode: String): String = {
    if (RailNetwork.stations.isDefinedAt(stationCode)) {
      RailNetwork.stations(stationCode).title.getOrElse(stationCode)
    } else {
      stationCode
    }
  }
  
  def hasLocation: Boolean = {
    val locZero = ((location != None) && (location.get._1 == 0d) && (location.get._2 == 0d))
    val locNone = (location == None)
    !(locZero || locNone)
  }
  
  override def toString = 
    "------------------------------" + Properties.lineSeparator +
    "Train:        " + getTitle + " (" + getGuid + ")" + Properties.lineSeparator +
    "Distance:     " + getDistanceInKm + " km" + Properties.lineSeparator +
    "Speed:        " + getSpeed + " km/h" + Properties.lineSeparator +
    "Heading:      " + getHeading + Properties.lineSeparator +
    "Route:        " + getFirstStation + " -> " + getLastStation + Properties.lineSeparator +
    "Stage:        " + getPreviousStation + " -> " + getNextStation + Properties.lineSeparator +
    "History size: " + history.size + " (" + getHistoryDistance + " km)" + Properties.lineSeparator +
    "Jammed ratio: " + getJammedRatio + Properties.lineSeparator +
    "------------------------------" + Properties.lineSeparator
        
  private def getHistoryDistance: Int = {
    if (history.size > 1) TrainDistanceCalculator.countDistance(history.head, history.last)
    else 0
  }
    
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
  
  private def getLastStation: String = {
    if (jammed) {
      "JAMMED"
    } else {
      stationTitle(ripStationCode(lastStation))
    }
  }
  
  private def getNextStation: String = {
    if (jammed) {
      "JAMMED"
    } else {
      stationTitle(ripStationCode(nextStation))
    }
  }
  
  private def getPreviousStation: String = {
    if (jammed) {
      "JAMMED"
    } else {
      stationTitle(ripStationCode(previousStation))
    }
  }

  private def getFirstStation: String = {
    if (jammed) {
      "JAMMED"
    } else {
      stationTitle(ripStationCode(firstStation))
    }
  }
  
  private def getJammedRatio: String = {
    val jr100 = jammedRatio * jammedRatioSampleSize
    jr100.toInt.toString + "/" + jammedRatioSampleSize
  }
  
}