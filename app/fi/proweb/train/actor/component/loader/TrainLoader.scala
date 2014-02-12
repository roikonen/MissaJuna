package fi.proweb.train.actor.component.loader

import fi.proweb.train.actor.component.DataLoader
import akka.actor.ActorRef
import akka.actor.Props
import fi.proweb.train.actor.component.validator.TrainDataValidator
import fi.proweb.train.actor.component.formatter.TrainFormatter
import fi.proweb.train.model.app.Train
import scala.collection.mutable.Queue
import fi.proweb.train.helper.DistanceCalculator

object TrainLoader {
  def props(trainGuid: String): Props = Props(new TrainLoader("http://188.117.35.14/TrainRSS/TrainService.svc/trainInfo?train=" + trainGuid))
}

class TrainLoader(url: String) extends DataLoader[Train](Props[TrainDataValidator], Props[TrainFormatter], url) {
  
  val TRY_BEFORE_JAMMED = 10
  val GATHER_TRAIN_HISTORY_IN_KM = 5
  val HISTORY_DATA_MAX_SIZE = 50
  
  private var historyData = Queue[Train]()
  private var jammed = 0
  
  def process(train: Train) {
    
    val oldest = oldestTrain(train)
    val latest = latestTrain(train)
    
    val oldestPoint = ripLocation(oldest)
    val latestPoint = ripLocation(latest)
    
    // IF train has location data AND (
    //  IF 
    //   (train's queue IS empty) OR
    //   (new train location IS 50 meters further than the previous train location)
    // )
    if (train.location.get != (0d, 0d) && (latest == None || distanceOfTrains(latest.get, train) > 50)) {
      historyData += train
      removeFromJammed
    }
    
    if (train.location.get != (0d, 0d) && (latest != None && distanceOfTrains(latest.get, train) <= 50)) {
      historyData.last.speed = train.speed
    }
 
    // IF train has no location data OR previous train had exactly the same location...
    if (train.location.get == (0d, 0d) || latest != None && latest.get.location.get == train.location.get) {
      jammedOnce
      if (historyData.size > 1 && isJammed) {
        historyData.clear
        historyData += train
      }
    }
    
//    if (isJammed) {
//      println("Size:       " + historyData.size + " (" + train.guid.get + ") (" + DistanceCalculator.countDistance(oldestPoint._1, oldestPoint._2, latestPoint._1, latestPoint._2) + ") (Jammed)")
//    } else {
//      println("Size:       " + historyData.size + " (" + train.guid.get + ") (" + DistanceCalculator.countDistance(oldestPoint._1, oldestPoint._2, latestPoint._1, latestPoint._2) + ")")
//    }
    
    if (historyData.size > HISTORY_DATA_MAX_SIZE || (historyData.size > 2 && DistanceCalculator.countDistance(oldestPoint._1, oldestPoint._2, latestPoint._1, latestPoint._2) > GATHER_TRAIN_HISTORY_IN_KM * 1000)) {
      historyData.dequeue
//      println("Size after: " + historyData.size + " (" + train.guid.get + ")")
    }
    
    train.history = historyData
    train.jammed = isJammed
    
  }
  
  private def latestTrain(train: Train): Option[Train] = {
    historyData.lastOption
  }
  
  private def oldestTrain(train: Train): Option[Train] = {
    historyData.headOption
  }
  
  private def ripLocation(train: Option[Train]): (Double, Double) = {
    if (train == None) {
      (0d, 0d)
    } else {
      train.get.location.getOrElse(0d, 0d)
    }
  }
  
  private def distanceOfTrains(train1: Train, train2: Train): Int = {
    DistanceCalculator.countDistance(train1.location.get._1, train1.location.get._2, train2.location.get._1, train2.location.get._2)
  }
  
  private def jammedOnce {
    jammed += 1
  }
  
  private def removeFromJammed {
    jammed = 0
  }
    
  private def isJammed: Boolean = {
    jammed > TRY_BEFORE_JAMMED
  }
  
}