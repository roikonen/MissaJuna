package fi.proweb.train.actor.component.formatter

import fi.proweb.train.actor.component.DataFormatter
import fi.proweb.train.model.app.TrainList

class TrainListFormatter extends DataFormatter[TrainList] {

  override def format(loadData: String): TrainList = {
    new TrainList
  }
  
}