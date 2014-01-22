package fi.proweb.train.model.app

import fi.proweb.train.model.AppData

class TrainStation extends AppData[TrainStation] {

  var guid: Option[String] = None

  var title: Option[String] = None

  var stationCode: Option[String] = None
  
  // Train has passed this station
  var completed: Option[Boolean] = None

  override def toString = title.getOrElse("?") + " (" + guid.getOrElse("?") + ")"
}