package fi.proweb.train.actor.core

import models.TrainPoint
import akka.actor.Actor
import akka.actor.ActorLogging
import fi.proweb.train.model.app.TrainList
import fi.proweb.train.model.app.Train
import java.util.Date
import java.math.BigDecimal

case object Record
case object StopRecordig
case class Trains(list: TrainList)

class TrainRecorder extends Actor with ActorLogging {

  // The minimum distance in meters between two points from the same train
  val radius = 50
  
  var recording: Boolean = false
    
  def receive = {
    case Record => recording = true
    case StopRecordig => recording = false
    case Trains(list: TrainList) => record(list)
  }
  
  def record(list: TrainList) {
    list.trains.values.foreach {
      train: Train => 
        val trainPoint = TrainPoint(None, new BigDecimal(train.location.get._1.toString), new BigDecimal(train.location.get._2.toString), train.guid.get, new Date)
        val locLat = trainPoint.locLat.toString().toDouble
        val locLon = trainPoint.locLon.toString().toDouble
        if (locLat != 0d && locLon != 0d) {
	        if (!TrainPoint.exists(trainPoint, radius)) {
	          println("Saving location    : " + trainPoint)
	          TrainPoint.create(trainPoint)
	        } else {
	          println("Not saving location: " + trainPoint)
	          log.debug("Nearby train point for the same train (" + trainPoint.trainGuid + ") already exists")
	        }
        }
    }
  }
  
}