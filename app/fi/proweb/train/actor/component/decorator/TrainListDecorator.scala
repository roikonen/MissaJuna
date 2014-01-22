package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.model.app.TrainList

class TrainListDecorator extends AppDataDecorator[TrainList] {

  override def decorate(trainList: TrainList): TrainList = {
    trainList
  }
  
}