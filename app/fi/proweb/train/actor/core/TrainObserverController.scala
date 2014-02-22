package fi.proweb.train.actor.core

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import scala.collection.mutable.Map
import scala.concurrent.duration._
import play.libs.Akka
import akka.actor.PoisonPill
import akka.actor.Props
import fi.proweb.train.model.app.Train
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import fi.proweb.train.model.app.TrainStation
import akka.actor.Cancellable

case class CreateObserver(locLat: Double, locLon: Double)
case class ObserverCreated(id: Long)
case class GetObserversTraintable(id: Long)
case class Traintable(traintable: List[Train])
case class Observer(observer: ActorRef)
case object CleanObservers
case class Refresh(observerId: Long, loc: (Double, Double))

object TrainObserverController {
  def props(trainListController: ActorRef, trainLoaderController: ActorRef): Props = Props(new TrainObserverController(trainListController, trainLoaderController))
}

class TrainObserverController(val trainListController: ActorRef, val trainController: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)
  
  private val inactiveObserverTimeToLive = 30 seconds

  private val cleanInterval = inactiveObserverTimeToLive
  private val cleanScheduler = context.system.scheduler.schedule(cleanInterval, cleanInterval, context.self, CleanObservers)(context.system.dispatcher, ActorRef.noSender)
  
  private val refreshInterval = 5 minutes
  
  private var observerIdCounter = 0L
  private var observers = Map[Long, (ActorRef, Deadline, Cancellable)]()
  
  def receive = {
    case CreateObserver(locLat: Double, locLon: Double) => createObserver(locLat, locLon, sender)
    case GetObserversTraintable(id: Long) => getTraintable(id, sender)
    case CleanObservers => cleanObservers
    case Refresh(observerId: Long, loc: (Double, Double)) => reRegister(observers(observerId)._1, loc._1, loc._2)
  }
  
  def createObserver(locLat: Double, locLon: Double, sender: ActorRef) {
    val observerId = observerIdCounter
    observerIdCounter += 1L
    
    val observer = Akka.system().actorOf(TrainObserver.props(trainController, locLat, locLon), "TrainObserver_" + observerId)
    trainListController.tell(Register(locLat, locLon), observer)
    
    val refresher = context.system.scheduler.schedule(refreshInterval, refreshInterval, context.self, Refresh(observerId, (locLat, locLon)))(context.system.dispatcher, ActorRef.noSender)
    
    observers += (observerId -> (observer, inactiveObserverTimeToLive fromNow, refresher))
    sender ! ObserverCreated(observerId)
  }
  
  def reRegister(observer: ActorRef, locLat: Double, locLon: Double) {
    log.debug("TrainObsrvrCtrl: Re-registering observer: " + observer)
    trainListController.tell(Register(locLat, locLon), observer)
  }
  
  def getTraintable(id: Long, requester: ActorRef) {
    val observerTuple = observers.get(id)
    if (observerTuple == None) {
      requester ! Traintable(List[Train]())
    } else {
      val observer = observerTuple.get._1
      val reRegisterer = observerTuple.get._3
      observers += (id -> (observer, inactiveObserverTimeToLive fromNow, reRegisterer))
      val future = (observer ? GetTraintable).mapTo[Traintable]
      future pipeTo requester
    }
  }
  
  def cleanObservers {
    observers = observers.filterNot {
      case (id: Long, (observer: ActorRef, deadline: Deadline, reRegisterer: Cancellable)) => {
        if (deadline.isOverdue) {
          log.debug("Timeout for observer: " + observer)
          reRegisterer.cancel
          trainListController.tell(Unregister, observer)
          observer ! PoisonPill
        }
        deadline.isOverdue
      }
    }
  }
  
}