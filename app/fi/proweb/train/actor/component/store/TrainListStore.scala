package fi.proweb.train.actor.component.store

import fi.proweb.train.actor.component.AppDataStore
import fi.proweb.train.model.app.TrainList
import fi.proweb.train.actor.component.AppDataMsg
import akka.actor.Props
import fi.proweb.train.actor.component.decorator.TrainListDecorator

class TrainListStore extends AppDataStore[TrainList](Props[TrainListDecorator]) {
  
  def receive = commonOp orElse trainStoreOp
  
  def trainStoreOp: PartialFunction[Any, Unit] = {
    case x => 
  }
  
  override def store(trainList: TrainList) = {
    println("Tallennetaan junalista")
  }
    
}