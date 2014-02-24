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

// IN
case class GetTraintable(locLat: Double, locLon: Double)
case object CleanObservers
private case class RefreshObserver(locLat: Double, locLon: Double)

// OUT
case class Traintable(traintable: List[Train])

object TrainObserverController {
  def props(trainListController: ActorRef, trainLoaderController: ActorRef): Props = Props(new TrainObserverController(trainListController, trainLoaderController))
}

class TrainObserverController(val trainListController: ActorRef, val trainController: ActorRef) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5 seconds)
  
  private val inactiveObserverTimeToLive = 1 minute

  private val cleanInterval = inactiveObserverTimeToLive
  private val cleanScheduler = context.system.scheduler.schedule(cleanInterval, cleanInterval, context.self, CleanObservers)(context.system.dispatcher, ActorRef.noSender)
  
  private val refreshInterval = 5 minutes
  
  private var observers = Map[(Double, Double), (ActorRef, Deadline, Cancellable)]()
  
  def receive = {
    case GetTraintable(locLat: Double, locLon: Double) => getTraintable(locLat: Double, locLon: Double, sender)
    case CleanObservers => cleanObservers
    case RefreshObserver(locLat: Double, locLon: Double) => reRegister(observers(locLat, locLon)._1, locLat, locLon)
  }
  
  def createObserver(locLat: Double, locLon: Double): ActorRef = {
    val observer = Akka.system().actorOf(TrainObserver.props(trainController, locLat, locLon), "TrainObserver_" + locLat + "_" + locLon)
    trainListController.tell(Register(locLat, locLon), observer)
    
    val refresher = context.system.scheduler.schedule(refreshInterval, refreshInterval, context.self, RefreshObserver(locLat, locLon))(context.system.dispatcher, ActorRef.noSender)
    
    observers += ((locLat, locLon) -> (observer, inactiveObserverTimeToLive fromNow, refresher))
    observer
  }
  
  def reRegister(observer: ActorRef, locLat: Double, locLon: Double) {
    log.debug("TrainObsrvrCtrl: Re-registering observer: " + observer)
    trainListController.tell(Register(locLat, locLon), observer)
  }

  def getTraintable(locLat: Double, locLon: Double, requester: ActorRef) {
    val observer = {
      if (!observers.contains(locLat, locLon)) {
        createObserver(locLat, locLon)
      } else {
        observers(locLat, locLon)._1
      }
    }
    val refresher = observers(locLat, locLon)._3
    observers += ((locLat, locLon) -> (observer, inactiveObserverTimeToLive fromNow, refresher))
    val future = (observer ? GetObserverTraintable).mapTo[Traintable]
    future pipeTo requester
  }
  
  def cleanObservers {
    observers = observers.filterNot {
      case (id: (Double, Double), (observer: ActorRef, deadline: Deadline, refresher: Cancellable)) => {
        if (deadline.isOverdue) {
          log.debug("Timeout for observer: " + observer)
          refresher.cancel
          trainListController.tell(Unregister, observer)
          observer ! PoisonPill
        }
        deadline.isOverdue
      }
    }
  }
  
}