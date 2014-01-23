package models

import anorm._
import anorm.SqlParser._
import play.api.libs.json._
import play.api.db._
import play.api.Play.current
import java.util.Date
import java.text.SimpleDateFormat
import java.text.Format

case class TrainPoint(id: Option[Long], locLat: Double, locLon: Double, trainGuid: String, updated: Date)

object TrainPoint {
  val formatter: Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  // Maps project into JSon
  implicit val projectWrites = new Writes[TrainPoint] {
    def writes(tp: TrainPoint): JsValue = {
      Json.obj(
        "id" -> tp.id,
        "locLat" -> tp.locLat,
        "locLon" -> tp.locLon,
        "trainGuid" -> tp.trainGuid,
        "updated" -> formatter.format(tp.updated)
        )
    }
  }

  // Parser for parsing from SQL to object.
  val trainPoint = {
    get[Option[Long]]("id") ~
      get[Double]("locLat") ~
      get[Double]("locLon") ~ 
      get[String]("trainGuid") ~
      get[Date]("updated") map {
        case id ~ locLat ~ locLon ~ trainGuid ~ updated => 
          TrainPoint(id, locLat, locLon, trainGuid, updated)
      }
  }
  
  def process(trainPoint: TrainPoint): Option[Long] = {
    if (trainPoint.locLat != 0d && trainPoint.locLon != 0d) {
      println("Tallennetaan sijainti: " + trainPoint)
      create(trainPoint)
    } else {
      None
    }
  }
  
  def create(trainPoint: TrainPoint): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO trainpoint(locLat, locLon, trainGuid, updated) VALUES ({locLat}, {locLon}, {trainGuid}, {updated})").on('locLat -> trainPoint.locLat).on('locLon -> trainPoint.locLon).on('trainGuid -> trainPoint.trainGuid).on('updated -> trainPoint.updated).executeInsert()
    }
  }
  
  def exists(trainPoint: TrainPoint, radius: Int): Boolean = {
    false
  }
  
  // TODO: Lataa kaikki tietyn junan pisteet, joiden 
  // locLat:n maksimiarvo on locLatMax ja locLat:n minimiarvo on locLatMin
  // locLon:n maksimiarvo on locLonMax ja locLon:n minimiarvo on locLonMin
  // Järjestyksessä locLat, locLon
  
}