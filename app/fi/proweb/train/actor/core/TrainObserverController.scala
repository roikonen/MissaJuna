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

case class CreateObserver(locLat: Double, locLon: Double)
case class ObserverCreated(id: Long)
case class GetObserversTraintable(id: Long)
case class Traintable(traintable: List[Train])
case class Observer(observer: ActorRef)
case object CleanObservers

object TrainObserverController {
  def props(trainListController: ActorRef, trainLoaderController: ActorRef): Props = Props(new TrainObserverController(trainListController, trainLoaderController))
}

class TrainObserverController(val trainListController: ActorRef, val trainController: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)
  
  private val inactiveObserverTimeToLive = 30 seconds
  
  private var observerIdCounter = 0L
  private var observers = Map[Long, (ActorRef, Deadline)]()
  
  private val cleanInterval = inactiveObserverTimeToLive
  private val cleanScheduler = context.system.scheduler.schedule(cleanInterval, cleanInterval, context.self, CleanObservers)(context.system.dispatcher, ActorRef.noSender)
  
  def receive = {
    case CreateObserver(locLat: Double, locLon: Double) => createObserver(locLat, locLon, sender)
    case GetObserversTraintable(id: Long) => getTraintable(id, sender)
    case CleanObservers => cleanObservers
  }
  
  def createObserver(locLat: Double, locLon: Double, sender: ActorRef) {
    val observerId = observerIdCounter
    observerIdCounter += 1L
    
    val observer = Akka.system().actorOf(TrainObserver.props(trainController, locLat, locLon), "TrainObserver_" + observerId)
    trainListController.tell(Register(new TrainStation(), new TrainStation()), observer)
    
    observers += (observerId -> (observer, inactiveObserverTimeToLive fromNow))
    sender ! ObserverCreated(observerId)
  }
  
  def getTraintable(id: Long, requester: ActorRef) {
    val observerTuple = observers.get(id)
    if (observerTuple == None) {
      requester ! Traintable(List[Train]())
    } else {
      val observer = observerTuple.get._1
      observers += (id -> (observer, inactiveObserverTimeToLive fromNow))
      val future = (observer ? GetTraintable).mapTo[Traintable]
      future pipeTo requester
    }
  }
  
  def cleanObservers {
    observers = observers.filterNot {
      case (id: Long, (observer: ActorRef, deadline: Deadline)) => {
        if (deadline.isOverdue) {
          log.debug("Timeout for observer: " + observer)
          trainListController.tell(Unregister, observer)
          observer ! PoisonPill
        }
        deadline.isOverdue
      }
    }
  }
  
}