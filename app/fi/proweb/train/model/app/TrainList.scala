package fi.proweb.train.model.app

import fi.proweb.train.model.AppData
import scala.collection.mutable.Map

class TrainList extends AppData[TrainList] {

  var trains = Map[String, Train]()
  
  def makeCopy: TrainList = {
    val trainlist = new TrainList
    
    trainlist.trains = Map[String, Train]()
    trains.foreach { t => 
      trainlist.trains += ((t._1, t._2.makeCopy))
    }
    
    trainlist
  }
  
}