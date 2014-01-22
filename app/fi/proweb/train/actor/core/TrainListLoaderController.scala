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

case class Register(track: (TrainStation, TrainStation))
case object Unregister

object TrainListLoaderController {
  def props(trainLoaderController: ActorRef): Props = Props(new TrainListLoaderController(trainLoaderController))
}

class TrainListLoaderController(val trainLoaderController: ActorRef) extends Actor with ActorLogging {

  val trains = Map[String, Train]()
  
  //----- Will get better when TrainListController completes -----
  val allTrains = Set[String]("IC287")
  val trainsToObserve = Set[String]("IC287")
  //----- Will get better when TrainListController completes -----
  
  createAllTrains
  
  def receive = {
    case Register(track: (TrainStation, TrainStation)) => trainLoaderController.tell(SubscribeTrains(trainsToObserve), sender)
    case Unregister => trainLoaderController.tell(UnsubscribeTrains, sender)
    case AppDataMsg(train: Train) => trains += (train.guid.get -> train); log.debug("Train: " + train.title.get + " created.")
  }
  
  def createAllTrains {
    allTrains.foreach(trainLoaderController ! CreateTrain(_))
  }
}