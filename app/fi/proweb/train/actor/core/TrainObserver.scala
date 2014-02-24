package fi.proweb.train.actor.core

import akka.actor.Actor
import fi.proweb.train.model.app.Train
import akka.actor.ActorRef
import fi.proweb.train.actor.component.store.TrainStore
import akka.actor.Props
import scala.concurrent.duration._
import fi.proweb.train.model.app.TrainStation
import akka.actor.ActorLogging
import fi.proweb.train.actor.component.AppDataMsg
import scala.collection.mutable.Set
import fi.proweb.train.actor.component.store.GetStoreTraintable
import models.Traintable

case object SubscribeTraintable
case object UnsubscribeTraintable
case object GetObserverTraintable

object TrainObserver {
  def props(
    trainLoaderController: ActorRef, locLat: Double, locLon: Double): Props =
    Props(new TrainObserver(trainLoaderController, locLat, locLon))
}

class TrainObserver(
  val trainLoaderController: ActorRef, 
  val locLat: Double, val locLon: Double) extends Actor with ActorLogging {
  
  val store = context.actorOf(TrainStore.props(locLat, locLon))
  
  private val subscribers: Set[ActorRef] = Set()
  private var sendAlsoTo: Option[ActorRef] = None
  
  def receive = {
    case SubscribeTraintable => subscribeTraintable(sender)
    case UnsubscribeTraintable => unsubscribeTraintable(sender)
    case GetObserverTraintable => sendAlsoTo = Option(sender); store ! GetStoreTraintable
    case traintable: Traintable => redirectTraintable(traintable) 
    case schedule: ScheduleTrain => redirectSchedule(schedule)
    case msg => store.tell(msg, sender) // Redirect rest of the messages to store
  }
  
  def subscribeTraintable(subscriber: ActorRef) {
    subscribers += subscriber
  }
  
  def unsubscribeTraintable(subscriber: ActorRef) {
    subscribers.remove(subscriber)
  }
  
  // Redirect traintable push to observer's owner
  def redirectTraintable(traintable: Traintable) {
    log.debug("Received and redirecting traintable...")
    subscribers.foreach(_ ! traintable)
    sendAlsoTo.foreach(_ ! traintable)
    sendAlsoTo = None
  }
  
  def redirectSchedule(scheduleTrain: ScheduleTrain) {
    log.debug("Received and redirecting schedule request to TrainLoaderController...")
    trainLoaderController ! scheduleTrain
  }
}