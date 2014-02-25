package models

import util.Properties
import play.api.libs.json._

case class Train(
    guid: String, title: String, timestamp: String,
    locLat: Double, locLon: Double,
    distance: Int, speed: Int, heading: String, 
    first: String, previous: String, next: String, last: String,
    historySize: Int, historyLengthInM: Int,
    numJammed: Int, numSamples: Int
    ) {
  
  override def toString = 
    "------------------------------" + Properties.lineSeparator +
    "Train:        " + title + " (" + guid + ")" + Properties.lineSeparator +
    "Timestamp:    " + timestamp + Properties.lineSeparator +
    "Distance:     " + distance + " m" + Properties.lineSeparator +
    "Location:     " + locLat + "," + locLon + Properties.lineSeparator +
    "Speed:        " + speed + " km/h" + Properties.lineSeparator +
    "Heading:      " + heading + Properties.lineSeparator +
    "Route:        " + first + " -> " + last + Properties.lineSeparator +
    "Stage:        " + previous + " -> " + next + Properties.lineSeparator +
    "History size: " + historySize + " (" + historyLengthInM + " m)" + Properties.lineSeparator +
    "Jammed ratio: " + numJammed + "/" + numSamples + Properties.lineSeparator +
    "------------------------------" + Properties.lineSeparator
    
}
    
object Train {
  
  // Maps into JSon
  implicit val projectWrites = new Writes[Train] {
    def writes(train: Train): JsValue = {
      Json.obj(
        "guid" -> train.guid,
        "title" -> train.title,
        "timestamp" -> train.timestamp,
        "locLat" -> train.locLat,
        "locLon" -> train.locLon,
        "distance" -> train.distance,
        "speed" -> train.speed,
        "heading" -> train.heading,
        "first" -> train.first,
        "previous" -> train.previous,
        "next" -> train.next,
        "last" -> train.last,
        "historySize" -> train.historySize,
        "historyLength" -> train.historyLengthInM,
        "numJammed" -> train.numJammed,
        "numSamples" -> train.numSamples
        )
    }
  }
  
}