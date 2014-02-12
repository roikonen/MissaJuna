package fi.proweb.train.actor.core

import akka.actor.ActorLogging
import akka.actor.Actor
import fi.proweb.train.model.app.TrainStation
import akka.actor.ActorRef
import akka.actor.Props
import scala.concurrent.duration._
import fi.proweb.train.actor.component.AppDataMsg
import fi.proweb.train.model.app.Train
import scala.collection.mutable.Map
import fi.proweb.train.actor.component.Unsubscribe
import fi.proweb.train.actor.component.loader.TrainListLoader
import fi.proweb.train.model.app.TrainList
import fi.proweb.train.actor.component.Subscribe
import fi.proweb.train.actor.component.Start
import scala.collection.mutable.Queue
import models.TrainPoint
import controllers.Application

trait TrainListLoaderControllerMsg
case class Register(locLat: Double, locLon: Double) extends TrainListLoaderControllerMsg
case object Unregister extends TrainListLoaderControllerMsg

object TrainListLoaderController { 
  def props(trainLoaderController: ActorRef): Props = Props(new TrainListLoaderController(trainLoaderController))
}

class TrainListLoaderController(val trainLoaderController: ActorRef) extends Actor with ActorLogging {

  var allTrains = Set[String]()
  
  val loader = context.actorOf(Props[TrainListLoader], "TrainListLoader")
  loader ! Subscribe
  loader ! Start(5 seconds)
    
  val recorder = context.actorOf(Props[TrainRecorder], "TrainRecorder")
  recorder ! Record
  
  val msgQ = Queue[(TrainListLoaderControllerMsg, ActorRef)]()
        
  def receive = {
    case Register(locLat: Double, locLon: Double) => register(locLat, locLon, sender)
    case Unregister => unRegister(Unregister, sender)
    case AppDataMsg(appdata) => appdata match {
      case train: Train => processMsgQ
      case trainList: TrainList => processTrainList(trainList)
    }
  }
  
  def register(locLat: Double, locLon: Double, sender: ActorRef) {
    if (allTrains.size == 0) {
      addToMsgQ(Register(locLat, locLon), sender)
    } else {
      val trainsToObserve = TrainPoint.findTrains(locLat, locLon, Application.OBSERVATION_RADIUS).filter(allTrains.contains(_))
      trainLoaderController.tell(SubscribeTrains(trainsToObserve.toSet), sender)
    }
  }
  
  def unRegister(msg: TrainListLoaderControllerMsg, sender: ActorRef) {
    if (allTrains.size == 0) {
      addToMsgQ(msg, sender)
    } else {
      trainLoaderController.tell(UnsubscribeTrains, sender)
    }
  }
  
  def addToMsgQ(msg: TrainListLoaderControllerMsg, sender: ActorRef) {
    msgQ.enqueue((msg, sender))
  }
  
  def processMsgQ {
    while (msgQ.nonEmpty) {
      val msg = msgQ.dequeue
      context.self.tell(msg._1, msg._2)
    }
  }
  
  def processTrainList(trainList: TrainList) {  

    recorder ! Trains(trainList)
    
    val oldTrains = allTrains
    allTrains = trainList.trains.keySet.toSet
    
    removedTrains(oldTrains, allTrains).foreach(trainLoaderController ! Freeze(_))
    newTrains(oldTrains, allTrains).foreach(trainLoaderController ! CreateOrMelt(_))
    
    processMsgQ
  }
    
  def newTrains(oldTrainSet: Set[String], newTrainSet: Set[String]): Set[String] = newTrainSet diff oldTrainSet
  
  def removedTrains(oldTrainSet: Set[String], newTrainSet: Set[String]): Set[String] = oldTrainSet diff newTrainSet
  
  def remainingTrains(oldTrainSet: Set[String], newTrainSet: Set[String]): Set[String] = oldTrainSet intersect newTrainSet

}
