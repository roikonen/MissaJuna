package fi.proweb.train.actor.component.loader

import akka.actor.ActorRef
import fi.proweb.train.actor.component.DataLoader
import akka.actor.Props
import fi.proweb.train.actor.component.validator.TrainListDataValidator
import fi.proweb.train.actor.component.formatter.TrainListFormatter
import fi.proweb.train.model.app.TrainList

class TrainListLoader extends DataLoader[TrainList](Props[TrainListDataValidator], Props[TrainListFormatter], 
    "http://188.117.35.14/TrainRSS/TrainService.svc/AllTrains") {
  
  def process(trainList: TrainList) {
    // Do nothing for now
  }
}