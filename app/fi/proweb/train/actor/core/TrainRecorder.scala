package fi.proweb.train.actor.core

import models.TrainPoint
import akka.actor.Actor
import akka.actor.ActorLogging
import fi.proweb.train.model.app.TrainList
import fi.proweb.train.model.app.Train
import java.util.Date

case object Record
case object StopRecordig
case class Trains(list: TrainList)

class TrainRecorder extends Actor with ActorLogging {

  var recording: Boolean = false
  
  def receive = {
    case Record => recording = true
    case StopRecordig => recording = false
    case Trains(list: TrainList) => record(list)
  }
  
  def record(list: TrainList) {
    list.trains.values.foreach {
      train: Train => 
        val trainPoint = TrainPoint(None, train.location.get._1, train.location.get._2, train.guid.get, new Date)
        val radius = 50
        if (!TrainPoint.exists(trainPoint, radius)) {
          TrainPoint.process(trainPoint)
        }        
    }
  }
  
}