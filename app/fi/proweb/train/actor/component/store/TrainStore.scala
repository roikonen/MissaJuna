package fi.proweb.train.actor.component.store

import fi.proweb.train.actor.component.AppDataStore
import fi.proweb.train.model.app.Train
import fi.proweb.train.actor.component.AppDataMsg
import akka.actor.Props
import fi.proweb.train.actor.component.decorator.TrainDecorator
import fi.proweb.train.actor.core.GetTraintable
import fi.proweb.train.actor.core.Traintable
import scala.collection.mutable.Map
import scala.concurrent.duration._
import akka.actor.Cancellable
import fi.proweb.train.actor.core.ScheduleTrain
import fi.proweb.train.helper.DistanceCalculator

object TrainStore {
  def props(locLat: Double, locLon: Double): Props = Props(new TrainStore(locLat, locLon))
}

class TrainStore(val locLat: Double, val locLon: Double) extends AppDataStore[Train](TrainDecorator.props(locLat, locLon)) {
    
  // How long to wait for other data before delivering forward
  val waitTime = 1 second
  
  private var traintable = List[Train]()
  
  private var trainData = Map[String, Train]()
  
  private var traintableScheduler = traintablePushScheduler
    
  def receive = commonOp orElse trainStoreOp
  
  def trainStoreOp: PartialFunction[Any, Unit] = {
    case GetTraintable => sender ! Traintable(traintable)
  }
  
  override def store(train: Train) = {
    
    trainData(train.guid.get) = train 
    adjustTrainLoaderScheduler(train)
    restartTraintablePushScheduler
  }
  
  def createTraintable: List[Train] = {
    
    var newTraintable: List[Train] = List()
    trainData.values.foreach {
      train: Train => 
        if (train.history.size > 1) {
          val oldest = train.history.headOption.get
          val latest = train.history.lastOption.get  
          if (TrainDecorator.countDistance(latest, (locLat, locLon)) < TrainDecorator.countDistance(oldest, (locLat, locLon))) {
            newTraintable = trainData(latest.guid.get) :: newTraintable
          }
        }
    }
    traintable = newTraintable sortBy(_.distance)
    traintable
  }
  
  def restartTraintablePushScheduler {
    traintableScheduler.cancel
    traintableScheduler = traintablePushScheduler
  }
    
  def adjustTrainLoaderScheduler(train: Train) {
    log.debug("Scheduling trainLoader from store...")
    if (train.jammed) schedule(train, 1 minute)
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
    // Chopped temporarily in debug purposes...
    val con = context
    val par = con.parent
    val tt1 = createTraintable
    val tt = Traintable(tt1)
    par ! tt
    //context.parent ! Traintable(createTraintable)
  }
  
  def schedule(train: Train, interval: FiniteDuration) {
    context.parent ! ScheduleTrain(train.guid.get, interval)
  }

}