package fi.proweb.train.actor.component.formatter

import fi.proweb.train.actor.component.DataFormatter
import fi.proweb.train.model.app.TrainList
import scala.xml.XML
import scala.xml.Node
import fi.proweb.train.model.app.Train

class TrainListFormatter extends DataFormatter[TrainList] {

  override def format(loadData: String): TrainList = {
    
    val trainList = new TrainList

    val rss = XML.loadString(loadData)
    val channel = rss \ "channel"
    
    val trains = (channel \\ "item")
    
    trains.foreach {
      trainNode: Node => {
        val train = new Train
        train.guid = Some((trainNode \ "guid").text)
        train.title = Some((trainNode \ "title").text)
        
        val location = (trainNode \ "point").text
        train.location = Some(extractLatLon(location))
        trainList.trains += (train.guid.get -> train)
      }
    }
    
    trainList
  }
  
}