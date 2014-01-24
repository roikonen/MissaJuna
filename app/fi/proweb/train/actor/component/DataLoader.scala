package fi.proweb.train.actor.component

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.ActorRef
import scala.io.Codec
import scala.concurrent._
import akka.actor.Props
import akka.actor.ActorLogging
import akka.pattern.pipe
import scala.collection.mutable.Set
import fi.proweb.train.model.AppData

case object Subscribe
case object Unsubscribe
case object Load // Load and deliver to subscribers
case object Get // Load and deliver to sender
case class LoadData(loadData: String)
case class DeliveryTarget(deliveryTarget: ActorRef)
case object Stop
case class Start(interval: FiniteDuration)
case class Schedule(interval: FiniteDuration)
case object Freeze
case object Melt

class DataLoader[T <: AppData[T]](val validatorProps: Props, val formatterProps: Props, val url: String) extends Actor with ActorLogging {

  import context.dispatcher
  
  private val scheduler = context.actorOf(Props[LoadScheduler])
  private val validator = context.actorOf(validatorProps)
  private val formatter = context.actorOf(formatterProps)
  
  validator ! DeliveryTarget(formatter)
  formatter ! DeliveryTarget(context.self)
  
  val CODEC = Codec.UTF8
  
  var subscribers = Set[ActorRef]()
  var deliverAlsoTo: Option[ActorRef] = None
    
  val cacheAge = 2 seconds
  var dataCache: Option[(Deadline, T)] = None
  var latestLoadFuture: Option[Future[LoadData]] = None
    
  var freezed = false
  
  var lastInterval: Option[FiniteDuration] = None
  
  def receive = {
    case Subscribe => subscribers.add(sender)
    case Unsubscribe => subscribers.remove(sender)
    case Load => deliverFromCacheOrLoad
    case Get => deliverAlsoTo = Some(sender); deliverFromCacheOrLoad
    case Invalid(invalid: String) =>  loadFailed(invalid)
    case Stop => scheduler ! Stop
    case Start(interval: FiniteDuration) => start(interval) 
    case Schedule(interval: FiniteDuration) => schedule(interval)
    case AppDataMsg(appdata: T) => deliverToSubscribers(appdata)
    case Freeze => freezed = true
    case Melt => freezed = false
  }
  
  def start(interval: FiniteDuration) {
    scheduler ! Start(interval)
    lastInterval = Some(interval)
  }
  
  def schedule(interval: FiniteDuration) {
    scheduler ! Schedule(interval)
    lastInterval = Some(interval)
  }
  
  def deliverFromCacheOrLoad = {
    if (!freezed) {
      if (getCache != None) {
        deliverToSubscribers(getCache.get)
      } else if (latestLoadFuture.forall(_.isCompleted)) {
        latestLoadFuture = Some(load)
        latestLoadFuture.get pipeTo validator
      }
    }
  }
  
  def load = future {
    log.debug("Starting to load url: " + url + "...")
    val loadData = scala.io.Source.fromURL(url)(CODEC).mkString
    log.debug("Loaded loadData from url: " + url + " (interval: " + lastInterval.getOrElse("None") + ")")
    LoadData(loadData)
  }
  
  def deliverToSubscribers(appData: T) {
        
    cache(appData)
    
    log.debug("Delivering: " + appData + " to " + subscribers.size + " subscribers")
    subscribers.foreach(_.tell(AppDataMsg(appData), context.parent))
    deliverAlsoTo.foreach(_.tell(AppDataMsg(appData), context.parent))
    deliverAlsoTo = None
  }  
  
  def cache(appData: T) {
    if (dataCache == None || dataCache.get._1.isOverdue) dataCache = Some(cacheAge fromNow, appData)
  }
  
  def getCache: Option[T] = {
    if (dataCache == None || dataCache.get._1.isOverdue) None
    else Option(dataCache.get._2)
  }
  
  def loadFailed(invalidLoadData: String) {
    // Do nothing with invalid load data for now
  }
}