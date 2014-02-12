package fi.proweb.train.actor.component.store

import fi.proweb.train.actor.component.AppDataStore
import fi.proweb.train.model.app.Train
import fi.proweb.train.actor.component.AppDataMsg
import akka.actor.Props
import fi.proweb.train.actor.component.decorator.TrainDecorator
import fi.proweb.train.actor.core.GetTraintable
import fi.proweb.train.actor.core.Traintable
import scala.collection.mutable.Queue
import scala.collection.mutable.Map
import scala.concurrent.duration._
import akka.actor.Cancellable
import fi.proweb.train.actor.core.ScheduleTrain
import fi.proweb.train.helper.DistanceCalculator

object TrainStore {
  def props(locLat: Double, locLon: Double): Props = Props(new TrainStore(locLat, locLon))
}

class TrainStore(locLat: Double, locLon: Double) extends AppDataStore[Train](TrainDecorator.props(locLat, locLon)) {
    
  // How long to wait for other data before delivering forward
  val waitTime = 1 second
  
  private var traintable = List[Train]()
  
  private var trainData = Map[String, Queue[Train]]()
  
  private var jammedTrains = Map[String, Int]()
  
  private var traintableScheduler = traintablePushScheduler
    
  def receive = commonOp orElse trainStoreOp
  
  def trainStoreOp: PartialFunction[Any, Unit] = {
    case GetTraintable => sender ! Traintable(traintable)
  }
  
  override def store(train: Train) = {
    
    val previous = previousTrain(train)
    
    val oldestPoint = ripLocation(trainData.getOrElse(train.guid.get, Queue[Train]()).headOption)
    val latestPoint = ripLocation(trainData.getOrElse(train.guid.get, Queue[Train]()).lastOption)
    
    // IF train already exist in trainData...
    if (!trainData.contains(train.guid.get)) {
      trainData += (train.guid.get -> Queue[Train]())
    }

    // IF train has location data AND (
    //  IF 
    //   (train's queue IS empty) OR
    //   (new train location IS 50 meters further than the previous train location)
    // )
    if (train.location.get != (0d, 0d) && (previous == None || distanceOfTrains(previous.get, train) > 50)) {
      trainData(train.guid.get) += train
      removeFromJammed(train)
    }
    
    if (train.location.get != (0d, 0d) && (previous != None && distanceOfTrains(previous.get, train) <= 50)) {
      trainData(train.guid.get).last.speed = train.speed
    }
 
    // IF train has no location data OR previous train had exactly the same location...
    if (train.location.get == (0d, 0d) || previous != None && previous.get.location.get == train.location.get) {
      addToJammed(train)
      if (trainData(train.guid.get).size > 1 && isJammed(train)) {
        trainData(train.guid.get).clear
        trainData(train.guid.get) += train
      }
    }
    
    if (isJammed(train)) {
      println("Size:       " + trainData(train.guid.get).size + " (" + train.guid.get + ") (" + DistanceCalculator.countDistance(oldestPoint._1, oldestPoint._2, latestPoint._1, latestPoint._2) + ") (Jammed)")
    } else {
      println("Size:       " + trainData(train.guid.get).size + " (" + train.guid.get + ") (" + DistanceCalculator.countDistance(oldestPoint._1, oldestPoint._2, latestPoint._1, latestPoint._2) + ")")
    }
    
    // IF queue's oldest and latest point have more than 1 km of distance...
    if (trainData(train.guid.get).size > 25 || (trainData(train.guid.get).size > 2 && DistanceCalculator.countDistance(oldestPoint._1, oldestPoint._2, latestPoint._1, latestPoint._2) > 1000)) {
      trainData(train.guid.get).dequeue
      println("Size after: " + trainData(train.guid.get).size + " (" + train.guid.get + ")")
    }
    
    adjustTrainLoaderScheduler(train)
    restartTraintablePushScheduler
  }
  
  def createTraintable: List[Train] = {
    
    var newTraintable: List[Train] = List()
    trainData.values.foreach {
      trainQ: Queue[Train] => 
        if (trainQ.size > 1) {
          val oldest = trainQ.headOption.get
          val latest = trainQ.lastOption.get
          if (latest.distance.get < oldest.distance.get) {
            newTraintable = latest :: newTraintable
          }
        }
    }
    traintable = newTraintable sortBy(_.distance)
    traintable
  }
  
  def previousTrain(train: Train): Option[Train] = {
    trainData.getOrElse(train.guid.get, Queue[Train]()).lastOption
  }
  
  def restartTraintablePushScheduler {
    traintableScheduler.cancel
    traintableScheduler = traintablePushScheduler
  }
    
  def adjustTrainLoaderScheduler(train: Train) {
    log.debug("Scheduling trainLoader from store...")
    if (isJammed(train)) schedule(train, 1 minute)
    else if (trainData(train.guid.get).size < 2) schedule(train, 5 seconds)
    else if(train.distance.get < 10 * 1000) schedule(train, 5 seconds)
    else if (train.distance.get < 20 * 1000) schedule(train, 10 seconds)
    else if (train.distance.get < 30 * 1000) schedule(train, 20 seconds)
    else if (train.distance.get < 100 * 1000) schedule(train, 30 seconds)
    else if (train.distance.get < 150 * 1000) schedule(train, 40 seconds)
    else schedule(train, 1 minute)
  }
  
  def traintablePushScheduler: Cancellable = {
    context.system.scheduler.scheduleOnce(waitTime)(deliverForward)(context.system.dispatcher)
  }
  
  def deliverForward = {
    log.debug("Delivering traintable to observer...")
    context.parent ! Traintable(createTraintable)
  }
  
  def schedule(train: Train, interval: FiniteDuration) {
    context.parent ! ScheduleTrain(train.guid.get, interval)
  }

  def ripLocation(train: Option[Train]): (Double, Double) = {
    if (train == None) {
      (0d, 0d)
    } else {
      train.get.location.getOrElse(0d, 0d)
    }
  }
  
  def addToJammed(train: Train) {
    if (jammedTrains.contains(train.guid.get)) {
      jammedTrains(train.guid.get) += 1
    } else {
      jammedTrains += (train.guid.get -> 1)
    }
  }
  
  def removeFromJammed(train: Train) {
    jammedTrains.remove(train.guid.get)
  }
    
  def isJammed(train: Train): Boolean = {
    jammedTrains.contains(train.guid.get) && jammedTrains(train.guid.get) > 10
  }
  
  def distanceOfTrains(train1: Train, train2: Train): Int = {
    DistanceCalculator.countDistance(train1.location.get._1, train1.location.get._2, train2.location.get._1, train2.location.get._2)
  }
}