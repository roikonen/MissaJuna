package models

import util.Properties
import play.api.libs.json._

case class Train(
    guid: String, title: String, 
    distance: Int, speed: Int, heading: String, 
    first: String, previous: String, next: String, last: String,
    historySize: Int, historyLengthInKm: Int,
    numJammed: Int, numSamples: Int
    ) {
  
  override def toString = 
    "------------------------------" + Properties.lineSeparator +
    "Train:        " + title + " (" + guid + ")" + Properties.lineSeparator +
    "Distance:     " + distance + " m" + Properties.lineSeparator +
    "Speed:        " + speed + " km/h" + Properties.lineSeparator +
    "Heading:      " + heading + Properties.lineSeparator +
    "Route:        " + first + " -> " + last + Properties.lineSeparator +
    "Stage:        " + previous + " -> " + next + Properties.lineSeparator +
    "History size: " + historySize + " (" + historyLengthInKm + " km)" + Properties.lineSeparator +
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
        "distance" -> train.distance,
        "speed" -> train.speed,
        "heading" -> train.heading,
        "first" -> train.first,
        "previous" -> train.previous,
        "next" -> train.next,
        "last" -> train.last,
        "historySize" -> train.historySize,
        "historyLength" -> train.historyLengthInKm,
        "numJammed" -> train.numJammed,
        "numSamples" -> train.numSamples
        )
    }
  }
  
}