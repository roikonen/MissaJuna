package fi.proweb.train.actor.component.loader

import fi.proweb.train.actor.component.DataLoader
import akka.actor.ActorRef
import akka.actor.Props
import fi.proweb.train.actor.component.validator.TrainDataValidator
import fi.proweb.train.actor.component.formatter.TrainFormatter
import fi.proweb.train.model.app.Train
import scala.collection.mutable.Queue
import fi.proweb.train.helper.TrainDistanceCalculator

object TrainLoader {
  def props(trainGuid: String): Props = Props(new TrainLoader("http://188.117.35.14/TrainRSS/TrainService.svc/trainInfo?train=" + trainGuid))
}

class TrainLoader(url: String) extends DataLoader[Train](Props[TrainDataValidator], Props[TrainFormatter], url) {
  
  val TRY_BEFORE_JAMMED = 10
  val GATHER_TRAIN_HISTORY_IN_KM = 5
  val HISTORY_DATA_MAX_SIZE = 50
  val MIN_DISTANCE_IN_M_BETWEEN_SUCCESSIVE_SAMPLES = 50
  val MAX_DISTANCE_IN_KM_BETWEEN_SUCCESSIVE_SAMPLES = 50
  
  private var historyData = Queue[Train]()
  private var jammed = 0
    
  override def afterFreeze {
    println("TrainLoader:     " + context.self + " freezed.")
    historyData.clear
  }
  
  override def afterMelt {
    println("TrainLoader:     " + context.self + " melted.")
  }
    
  def process(train: Train) {
    
    if (train.hasLocation) {
      if (hasSameLocationAsBefore(train)) {
        jammedOnce
        if (!isJammed) println("TrainLoader pro: Train " + train.guid.get + " has the same location as before. Jammed size: " + jammed)
      } else if (hasSuspiciousLocationFromBefore(train)) {
        jammedOnce
        if (!isJammed) println("TrainLoader pro: Train " + train.guid.get + " has suspicious location from before. Difference to latest: " + distanceOfTrains(latestTrain.get, train) + " m. Jammed size: " + jammed)
      } else if (hasMovedEnoughToGetTracked(train)) {
        if (jammed > 0) println("TrainLoader pro: Train " + train.guid.get + " added to the train history.")
        addTrainToHistory(train)
      } else {
        if (jammed > 0) println("TrainLoader pro: Train " + train.guid.get + " updated latest train in the train history.")
        updateLatestTrainDetailsWith(train)
      }
    } else { // Train has no location
      jammedOnce
      if (hasHistory) train.location = latestTrain.get.location
      if (!isJammed) println("TrainLoader pro: Train " + train.guid.get + " has no location data. Jammed size: " + jammed)
    }

//    if (historyData.size > 1) {
//      println("TrainLoader:     History size before: " + historyData.size + " (" + train.guid.get + ") (" + TrainDistanceCalculator.countDistance(oldestTrain.get, latestTrain.get) + ")")
//    }
    
    optimizeHistoryData
        
//    if (historyData.size > 1) {
//      println("TrainLoader:     History size after:  " + historyData.size + " (" + train.guid.get + ") (" + TrainDistanceCalculator.countDistance(oldestTrain.get, latestTrain.get) + ")")
//    }
    
    train.history = historyData
    train.jammed = isJammed
    
  }
    
  private def hasSuspiciousLocationFromBefore(train: Train): Boolean = {
    if (hasHistory) distanceOfTrains(latestTrain.get, train) > MAX_DISTANCE_IN_KM_BETWEEN_SUCCESSIVE_SAMPLES * 1000
    else false
  }
  
  private def hasMovedEnoughToGetTracked(train: Train): Boolean = {
    if (hasHistory) distanceOfTrains(latestTrain.get, train) > MIN_DISTANCE_IN_M_BETWEEN_SUCCESSIVE_SAMPLES
    else true
  }
  
  private def hasSameLocationAsBefore(train: Train): Boolean = {
    if (hasHistory) latestTrain.get.location.get == train.location.get
    else false
  }
  
  private def hasHistory: Boolean = {
    historyData.size > 0
  }
  
  private def addTrainToHistory(train: Train) {
    if (isJammed) removeFromJammed
    jammed = 0
    historyData += train
  }

  private def updateLatestTrainDetailsWith(train: Train) {
    if (isJammed) removeFromJammed
    jammed = 0
    if (hasHistory) {
      val latest = latestTrain
      latest.get.speed = train.speed
      latest.get.heading = train.heading
    }
  }
  
  private def optimizeHistoryData {
    while (historyData.size > HISTORY_DATA_MAX_SIZE || (historyData.size > 2 && TrainDistanceCalculator.countDistance(oldestTrain.get, latestTrain.get) > GATHER_TRAIN_HISTORY_IN_KM * 1000)) {
      historyData.dequeue
    }
  }
  
  private def latestTrain: Option[Train] = {
    historyData.lastOption
  }
  
  private def oldestTrain: Option[Train] = {
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
    TrainDistanceCalculator.countDistance(train1, train2)
  }

  private def jammedOnce {
    if (jammed == TRY_BEFORE_JAMMED) {
      println("TrainLoader:     " + context.self + " jammed")
      historyData.clear
    }
    jammed += 1
  }

  private def removeFromJammed {
    jammed = 0
    println("TrainLoader:     " + context.self + " not jammed anymore")
  }
    
  private def isJammed: Boolean = {
    jammed > TRY_BEFORE_JAMMED
  }
  
}