package fi.proweb.train.actor.core

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.ActorRef
import scala.collection.mutable.Map
import fi.proweb.train.actor.component.loader.TrainLoader
import akka.actor.Cancellable
import fi.proweb.train.actor.component.Stop
import fi.proweb.train.actor.component.Start
import akka.actor.ActorLogging
import fi.proweb.train.actor.component.Unsubscribe

case class SubscribeTrains(trains: Set[String])
case object UnsubscribeTrains
case class ScheduleTrain(train: String, interval: FiniteDuration)
case class Freeze(train: String)
case class CreateOrMelt(train: String)
case class Melt(train: String)

class TrainLoaderController extends Actor with ActorLogging {

  val trainLoaders = Map[String, ActorRef]()
  val observers = Map[ActorRef, Map[String, FiniteDuration]]()
  
  def receive = {
    case CreateOrMelt(train: String) => createOrMelt(train, sender)
    case SubscribeTrains(trains: Set[String]) => subscribe(sender, trains)
    case UnsubscribeTrains => unsubscribe(sender)
    case ScheduleTrain(train: String, interval: FiniteDuration) => schedule(sender, train, interval)
    case Freeze(train: String) => trainLoaders(train) ! fi.proweb.train.actor.component.Freeze
    case Melt(train: String) => trainLoaders(train) ! fi.proweb.train.actor.component.Melt
    case x => log.error("Not expected to receive: " + x)
  }
  
  def createOrMelt(train: String, requester: ActorRef) {
    if (trainLoaders.isDefinedAt(train)) {
      trainLoaders(train) ! fi.proweb.train.actor.component.Melt
    } else {
	    trainLoaders += (train -> context.actorOf(TrainLoader.props(train), "Trainloader_" + train))
	    trainLoaders(train).tell(fi.proweb.train.actor.component.Get, requester)
    }
  }
  
  def schedule(observer: ActorRef, train: String, interval: FiniteDuration) {
    if (!observers.isDefinedAt(observer)) observers += (observer -> Map(train -> interval))
    else observers(observer) += (train -> interval)
    schedule(train, observer)
  }
  
  def subscribe(requester: ActorRef, trains: Set[String]) {
    println("TrainLoaderCtrl: Subscribe request received from: " + requester)
    unsubscribeUnNeededTrains(requester, trains)
    trains.foreach(trainLoaders(_).tell(fi.proweb.train.actor.component.Subscribe, requester))
    trains.foreach(trainLoaders(_).tell(fi.proweb.train.actor.component.Get, requester))
  }
    
  def unsubscribe(observer: ActorRef) {
    val oldTrains = getTrainList(observer)
    observers.remove(observer)
    oldTrains.foreach(schedule(_, observer))
  }
  
  def unsubscribeUnNeededTrains(observer: ActorRef, newTrains: Set[String]) {
    val oldTrains = getTrainList(observer)
    val trainsForUnsubscription = oldTrains diff newTrains
    trainsForUnsubscription.foreach(observers(observer).remove(_))
    trainsForUnsubscription.foreach(schedule(_, observer))
    println("TrainLoaderCtrl: UnNeeded trains unsubscribed: " + trainsForUnsubscription)
  }
       
  def getTrainList(observer: ActorRef): Set[String] = {
    if (observers.contains(observer)) {
      observers(observer).keySet.toSet
  	} else {
  	  Set[String]()
  	}
  }
    
  def schedule(train: String, observer: ActorRef) {
    
    val trainIntervals = for {
      observer <- observers.values
      trainIntervalPair <- observer
      if trainIntervalPair._1 == train
    } yield trainIntervalPair
        
    if (trainIntervals.size == 0) {
      trainLoaders(train).tell(Unsubscribe, observer)
      trainLoaders(train) ! Stop
      println("TrainLoaderCtrl: Stopped train loader scheduler: " + train)
    } else {
      val newInterval = trainIntervals.minBy(_._2)._2
      trainLoaders(train) ! fi.proweb.train.actor.component.Schedule(newInterval)
//      println("TrainLoaderCtrl: Started train loader scheduler: " + train)
    }
  }

}