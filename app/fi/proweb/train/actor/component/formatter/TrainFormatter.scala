package fi.proweb.train.actor.component.formatter

import fi.proweb.train.actor.component.DataFormatter
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.HeadingConverter
import scala.xml.XML
import scala.xml.Node
import fi.proweb.train.model.app.TrainStation

class TrainFormatter extends DataFormatter[Train] {

  override def format(loadData: String): Train = {
    val rss = XML.loadString(loadData)
    val channel = rss \ "channel"
    
    val train = new Train
    train.guid = Some((channel \ "trainguid").text)
    train.title = Some((channel \ "title").text)
    train.lastBuildDate = Some((channel \ "lastBuildDate").text)

    val location = (channel \ "point").text
    train.location = Some(extractLatLon(location))
    
    val speed = (channel \ "speed").text
    train.speed = Some(speed.toInt)
    
    val heading = (channel \ "heading").text
    if (heading.length > 0) train.heading = Some(HeadingConverter.headingToString(heading.toInt))
    else train.heading = Some("N/A")
    
    val stations = (channel \\ "item")
    
    stations.foreach {
      stationNode: Node => {
        val trainStation = new TrainStation
        trainStation.guid = Some((stationNode \ "guid").text)
        trainStation.title = Some((stationNode \ "title").text)
        trainStation.stationCode = Some((stationNode \ "stationCode").text)
        
        val completed = (stationNode \ "completed").text
        trainStation.completed = Some(completed == "1")
        train.stations += trainStation
      }
    }
           
    train
  }
  
}