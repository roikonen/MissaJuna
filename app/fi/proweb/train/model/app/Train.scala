package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
import fi.proweb.train.data.RailNetwork
import util.Properties
import scala.collection.mutable.MutableList
import scala.collection.mutable.Queue
import fi.proweb.train.helper.TrainDistanceCalculator

class Train extends AppData[Train] {

  val NOT_AVAILABLE = "N/A"
  
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
  var numJammed = 0
  var numSamples = 0
    
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
    train.numJammed = numJammed
    train.numSamples = numSamples
        
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
              
  def getTitle: String = title.getOrElse(NOT_AVAILABLE)
    
  def getGuid: String = guid.getOrElse(NOT_AVAILABLE)
  
  def getLocLat: Double = location.getOrElse(0d, 0d)._1
  
  def getLocLon: Double = location.getOrElse(0d, 0d)._2
  
  def getDistanceInM: Int = distance.getOrElse(1000000)
  
  def getDistanceInKm: Double = distanceInKm.getOrElse(1000d)
  
  def getSpeed: Int = speed.getOrElse(0)
  
  def getHeading: String = heading.getOrElse(NOT_AVAILABLE)
  
  def getLastStation: String = stationTitle(ripStationCode(lastStation))
  
  def getNextStation: String = stationTitle(ripStationCode(nextStation))
  
  def getPreviousStation: String = stationTitle(ripStationCode(previousStation))

  def getFirstStation: String = stationTitle(ripStationCode(firstStation))
    
  def getHistorySize = history.size
  
  def getHistoryLengthInKm: Int = {
    if (history.size > 1) TrainDistanceCalculator.countDistance(history.head, history.last)
    else 0
  }
  
  def getNumJammed = numJammed
  
  def getNumSamples = numSamples
 
}