package fi.proweb.train.actor.component

import akka.actor.Actor
import akka.actor.ActorLogging
import fi.proweb.train.model.AppData
import akka.actor.Props
import akka.actor.ActorRef

case class AppDataMsg[T <: AppData[T]](appData: T)

abstract class AppDataStore[T <: AppData[T]](val decoratorProps: Props) extends Actor with ActorLogging {
  
  val decorator = context.actorOf(decoratorProps)
  
  decorator ! DeliveryTarget(context.self)
  
  def commonOp: PartialFunction[Any, Unit] = {
    case AppDataMsg(appData: T @unchecked) => decorateOrStore(appData, sender)
  }
    
  def decorateOrStore(appData: T, sender: ActorRef) {
    if (sender == decorator) store(appData)
    else decorate(appData)
  }
  
  def decorate(appData: T) {
    decorator ! AppDataMsg[T](appData)
  }
  
  def store(appData: T)
  
}