package fi.proweb.train.model.app

import fi.proweb.train.model.AppData

object TrainStation {
  def apply(stationCode: String, title: String): TrainStation = {
    val station = new TrainStation
    station.stationCode = Some(stationCode)
    station.title = Some(title)
    station
  }
}

class TrainStation extends AppData[TrainStation] {

  var guid: Option[String] = None

  var title: Option[String] = None

  var stationCode: Option[String] = None
  
  // Train has passed this station
  var completed: Option[Boolean] = None

  def makeCopy: TrainStation = {
    val trainstation = new TrainStation
    
    trainstation.guid = guid
    trainstation.title = title
    trainstation.stationCode = stationCode
    trainstation.completed = completed
    
    trainstation
  }
  
  override def toString = title.getOrElse("?") + " (" + guid.getOrElse("?") + ")"
}