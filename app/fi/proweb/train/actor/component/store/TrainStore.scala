package fi.proweb.train.actor.component.store

import fi.proweb.train.actor.component.AppDataStore
import fi.proweb.train.model.app.Train
import fi.proweb.train.actor.component.AppDataMsg
import akka.actor.Props
import fi.proweb.train.actor.component.decorator.TrainDecorator
import fi.proweb.train.actor.core.GetTraintable
import fi.proweb.train.actor.core.Traintable
import scala.collection.mutable.Queue
import scala.concurrent.duration._
import akka.actor.Cancellable
import fi.proweb.train.actor.core.ScheduleTrain

object TrainStore {
  def props(locLat: Double, locLon: Double): Props = Props(new TrainStore(locLat, locLon))
}

class TrainStore(locLat: Double, locLon: Double) extends AppDataStore[Train](TrainDecorator.props(locLat, locLon)) {
    
  // How long to wait for other data before delivering forward
  val waitTime = 1 second
  
  var traintable = List[Train]()
  
  var trainData: Map[String, Queue[Train]] = Map[String, Queue[Train]]()
  
  var traintableScheduler = traintablePushScheduler
    
  def receive = commonOp orElse trainStoreOp
  
  def trainStoreOp: PartialFunction[Any, Unit] = {
    case GetTraintable => sender ! Traintable(traintable)
  }
  
  override def store(train: Train) = {
    if (!trainData.contains(train.guid.get)) {
      trainData += (train.guid.get -> Queue[Train]())
    }

    if (lastTrain(train) == None || lastTrain(train).get.location.get != train.location.get) {
      trainData(train.guid.get) += train
    }

    if (trainData(train.guid.get).size == 3) {
      trainData(train.guid.get).dequeue
    }
    
    adjustTrainLoaderScheduler(train)
    restartTraintablePushScheduler
  }
  
  def createTraintable: List[Train] = {
    // Otetaan mukaan myös junat jotka liikkuvan max 1m/s (jos ottoväli 5s) pois päin 
    // (gps:n epätarkkuudesta johtuen)
    var newTraintable: List[Train] = List()
    trainData.values.foreach {
      trainQ: Queue[Train] => 
        if (trainQ.size == 2 && (trainQ(1).distance.get) < trainQ(0).distance.get + 5) {
          newTraintable = trainQ(1) :: newTraintable
        }
    }
    traintable = newTraintable sortBy(_.distance)
    traintable
  }
  
  def lastTrain(train: Train): Option[Train] = {
    trainData.getOrElse(train.guid.get, Queue[Train]()).toIterable.lastOption
  }
  
  def restartTraintablePushScheduler {
    traintableScheduler.cancel
    traintableScheduler = traintablePushScheduler
  }
    
  def adjustTrainLoaderScheduler(train: Train) {
    log.debug("Scheduling trainLoader from store...")
    if (trainData(train.guid.get).size < 2) schedule(train, 5 seconds)
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
}