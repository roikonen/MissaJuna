package models

import util.Properties
import play.api.libs.json._

case class Traintable(msg: String, trains: List[Train]) {

  override def toString = "Message: " + msg + Properties.lineSeparator + Properties.lineSeparator +
    (trains mkString (Properties.lineSeparator + Properties.lineSeparator))

}

object Traintable {
  
  // Maps into JSon
  implicit val projectWrites = new Writes[Traintable] {
    def writes(traintable: Traintable): JsValue = {
      Json.obj(
        "message" -> traintable.msg,
        "trains" -> traintable.trains
        )
    }
  }
  
}