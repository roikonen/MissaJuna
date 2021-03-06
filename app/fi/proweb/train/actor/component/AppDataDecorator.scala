package fi.proweb.train.actor.component

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorLogging
import fi.proweb.train.model.AppData

case class Decorate[T <: AppData[T]](appData: T)

abstract class AppDataDecorator[T <: AppData[T]] extends Actor with ActorLogging {

  var deliveryTarget: Option[ActorRef] = None
  
  def receive = receiveCommon orElse receiveUnchecked
  
  def receiveCommon: PartialFunction[Any, Unit] = {
    case DeliveryTarget(target) => deliveryTarget = Some(target)
  }

  def receiveUnchecked: PartialFunction[Any, Unit] = {
    case AppDataMsg(appData: T @unchecked) => decorateAndDeliver(appData)
  }
  
  def decorateAndDeliver(appData: T) {
    if (deliveryTarget == None) {
      log.error("Empty Delivery target when trying to deliver")
    } else {
      log.debug("Decorating and delivering appData to " + deliveryTarget)
      deliveryTarget.get ! AppDataMsg[T](decorate(appData))
    }   
  }
  
  def decorate(appData: T): T
  
}