package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
import scala.collection.mutable.Map

class TrainList extends AppData[TrainList] {

  val trains = Map[String, Train]()
  
}