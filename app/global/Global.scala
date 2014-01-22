package global

import play.api._
import play.libs.Akka
import akka.actor.Props
import akka.actor.PoisonPill
import akka.pattern.{ ask }
import fi.proweb.train.actor.core.TrainLoaderController
import fi.proweb.train.actor.core.TrainListLoaderController
import fi.proweb.train.actor.core.TrainObserverController
import akka.actor.ActorRef
import fi.proweb.train.actor.core.CreateObserver
import scala.concurrent.Future
import fi.proweb.train.actor.core.ObserverCreated
import akka.util.Timeout
import scala.concurrent.duration._

object Global extends GlobalSettings {

  // The location of the observer
  val locLat = 61.348997
  val locLon = 23.761861
  
  private val trainController = Akka.system().actorOf(Props[TrainLoaderController], "TrainLoaderController")
  private val trainListController = Akka.system().actorOf(TrainListLoaderController.props(trainController), "TrainListLoaderController")
  val trainObserverController =  Akka.system().actorOf(TrainObserverController.props(trainListController, trainController), "TrainObserverController")
  
  implicit val timeout = Timeout(5 seconds)
 
  def createObserver: Future[Any] = {
    trainObserverController ? CreateObserver(locLat, locLon) 
  }
  
  def getObserver(id: Long): Option[ActorRef] = {
    None
  }
  
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }  

  override def onStop(app: Application) {
    trainListController ! PoisonPill
    trainController ! PoisonPill
    Logger.info("Application shutdown...")
  }  
}