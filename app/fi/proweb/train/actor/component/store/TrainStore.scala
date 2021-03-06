package fi.proweb.train.actor.component.store

import fi.proweb.train.actor.component.AppDataStore
import fi.proweb.train.model.app.Train
import fi.proweb.train.actor.component.AppDataMsg
import akka.actor.Props
import fi.proweb.train.actor.component.decorator.TrainDecorator
import fi.proweb.train.actor.core.GetTraintable
import scala.collection.mutable.Map
import scala.concurrent.duration._
import akka.actor.Cancellable
import fi.proweb.train.actor.core.ScheduleTrain
import fi.proweb.train.helper.TrainDistanceCalculator
import models.Traintable
import TrainStore._

case object GetStoreTraintable

object TrainStore {

  val TRAINTABLE_NOT_INITIALIZED = Traintable("Initializing...", List[models.Train]())
  val TRAINTABLE_NO_CONTENT = Traintable("Gathering data...", List[models.Train]())

  def props(locLat: Double, locLon: Double): Props = Props(new TrainStore(locLat, locLon))

  implicit def Trainlist2Traintable(trains: List[Train]): Traintable = {
    if (trains.size == 0) {
      TRAINTABLE_NO_CONTENT
    } else {
      Traintable("OK", trains.map(train =>
        models.Train(
          train.getGuid, train.getTitle, train.getTimestamp,
          train.getLocLat, train.getLocLon,
          train.getDistanceInM, train.getSpeed, train.getHeading,
          train.getFirstStation, train.getPreviousStation, train.getNextStation, train.getLastStation,
          train.getHistorySize, train.getHistoryLengthInM,
          train.getNumJammed, train.getNumSamples)))
    }
  }
}

class TrainStore(val locLat: Double, val locLon: Double) extends AppDataStore[Train](TrainDecorator.props(locLat, locLon)) {
    
  // How long to wait for other data before delivering forward
  val waitTime = 1 second

  private var traintable = TRAINTABLE_NOT_INITIALIZED
  
  private var trainData = Map[String, Train]()
  
  private var traintableScheduler = traintablePushScheduler
    
  def receive = commonOp orElse trainStoreOp
  
  def trainStoreOp: PartialFunction[Any, Unit] = {
    case GetStoreTraintable => sender ! traintable
  }

  override def store(train: Train) = {
//    println("TrainStore:      Received train: " + train.guid.get + " by " + context.self)
    trainData(train.guid.get) = train
    adjustTrainLoaderScheduler(train)
    restartTraintablePushScheduler
  }
    
  def createTraintable: Traintable = {
    
    var trainlist: List[Train] = List()
    trainData.values.foreach {
      train: Train => 
        if (train.history.size > 1) {
          val latest = train.history.last
          val distFromLatest = TrainDistanceCalculator.countDistance(latest, (locLat, locLon))
          val oldest = findOldest(distFromLatest, latest, train.history.toList).get
          val distFromOldest = TrainDistanceCalculator.countDistance(oldest, (locLat, locLon))
          if (distFromLatest < distFromOldest) {
            trainlist = trainData(latest.guid.get) :: trainlist
          }
        }
    }
    traintable = trainlist sortBy(_.distance)
    traintable
  }
  
  // When train is closer to the observer than train has history in meters, 
  // only take as much history in count as there is between observer and the train but min 1 km.
  def findOldest(max: Int, latest: Train, trainHistory: List[Train]): Option[Train] = trainHistory match {
    case head :: tail => {
      val dist = TrainDistanceCalculator.countDistance(latest, head)
      if (dist < max || dist < 1000) {
        Some(head)
      } else {
        findOldest(max, latest, tail)
      }
    }
    case Nil => None
  }
  
  def restartTraintablePushScheduler {
    traintableScheduler.cancel
    traintableScheduler = traintablePushScheduler
  }
    
  def adjustTrainLoaderScheduler(train: Train) {
    log.debug("Scheduling trainLoader from store...")
    if (train.jammed) schedule(train, 1 minute)
    else if (!train.hasLocation) schedule(train, 10 seconds)
    else if (train.history.size < 2) schedule(train, 5 seconds)
    else if (train.distance.get < 10 * 1000) schedule(train, 5 seconds)
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
    if (context == null) println("null context for: " + self)
    else context.parent ! createTraintable
  }
  
  def schedule(train: Train, interval: FiniteDuration) {
//    println("TrainStore:      Scheduling " + train.guid.get + " with interval of " + interval)
    context.parent ! ScheduleTrain(train.guid.get, interval)
  }

}