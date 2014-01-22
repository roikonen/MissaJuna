package fi.proweb.train.actor.component

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import fi.proweb.train.model.AppData

abstract class DataFormatter[T <: AppData[T]] extends Actor with ActorLogging {
  
  var deliveryTarget: Option[ActorRef] = None
  
  def receive = {
    case DeliveryTarget(target) => deliveryTarget = Some(target)
    case LoadData(loadData: String) => formatAndDeliver(loadData)
  }
    
  def formatAndDeliver(loadData: String) {
    if (deliveryTarget == None) {
      log.error("Empty Delivery target when trying to deliver")
    } else {
      log.debug("Formatting loadData and delivering appData to " + deliveryTarget)
      deliveryTarget.get.tell(AppDataMsg[T](format(loadData)), context.parent)
    }
  }
  
  def extractLatLon(location: String): (Double, Double) = {
    val pointLatLon = location.split(" ")
    val pointLat = pointLatLon(0).toDouble
    val pointLon = pointLatLon(1).toDouble
    (pointLat, pointLon)
  }
  
  def format(loadData: String): T
  
}