package models

import anorm._
import anorm.SqlParser._
import play.api.libs.json._
import play.api.db._
import play.api.Play.current
import java.util.Date
import java.text.SimpleDateFormat
import java.text.Format
import fi.proweb.train.helper.DistanceCalculator
import java.math.BigDecimal

case class TrainPoint(id: Option[Long], locLat: BigDecimal, locLon: BigDecimal, trainGuid: String)//, updated: Date)

object TrainPoint {
  val formatter: Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  // Maps project into JSon
  implicit val projectWrites = new Writes[TrainPoint] {
    def writes(tp: TrainPoint): JsValue = {
      Json.obj(
        "id" -> tp.id,
        "locLat" -> tp.locLat.toString().toDouble,
        "locLon" -> tp.locLon.toString().toDouble,
        "trainGuid" -> tp.trainGuid//,
        //"updated" -> formatter.format(tp.updated)
        )
    }
  }

  // Parser for parsing from SQL to object
  val trainPoint = {
    get[Option[Long]]("id") ~
      get[BigDecimal]("locLat") ~
      get[BigDecimal]("locLon") ~ 
      get[String]("trainGuid") map {//~
      //get[Date]("updated") map {
        case id ~ locLat ~ locLon ~ trainGuid => // ~ updated => 
          TrainPoint(id, locLat, locLon, trainGuid)//, updated)
      }
  }
    
  def create(trainPoint: TrainPoint): Option[Long] = {
    DB.withConnection { implicit connection =>
      SQL("INSERT INTO trainpoint(locLat, locLon, trainGuid) " + //, updated) " +
          "VALUES ({locLat}, {locLon}, {trainGuid})"//, {updated})"
          ).on('locLat -> trainPoint.locLat).on('locLon -> trainPoint.locLon).on('trainGuid -> trainPoint.trainGuid).executeInsert()//.on('updated -> trainPoint.updated).executeInsert()
    }
  }
  
  def countAll: Long = {
    DB.withConnection { implicit connection =>
      SQL("SELECT COUNT(*) as count FROM trainpoint").apply().head[Long]("count")
    }
  }
  
  def exists(trainPoint: TrainPoint, radius: Int): Boolean = {
    val trainPoints = find(trainPoint, radius)
    val locLat = trainPoint.locLat.toString().toDouble
    val locLon = trainPoint.locLon.toString().toDouble
    trainPoints.exists( tp => {
      DistanceCalculator.countDistance(locLat, locLon, tp.locLat.toString().toDouble, tp.locLon.toString().toDouble) <= radius
    })
  }

  def find(refTrainPoint: TrainPoint, radius: Int): List[TrainPoint] = {
    DB.withConnection { implicit connection =>
      val locLat = refTrainPoint.locLat.toString().toDouble
      val locLon = refTrainPoint.locLon.toString().toDouble
      val rectangle = DistanceCalculator.countRectangle((locLat, locLon), radius)
      SQL(
        "SELECT * FROM trainpoint WHERE " +
          "trainguid = {trainguid} AND " +
          "locLat <= {locLatMax} AND " +
          "locLat >= {locLatMin} AND " +
          "locLon <= {locLonMax} AND " +
          "locLon >= {locLonMin}").on(
          'trainguid -> refTrainPoint.trainGuid,
          'locLatMax -> new BigDecimal(rectangle._1._1.toString),
          'locLatMin -> new BigDecimal(rectangle._2._1.toString),
          'locLonMax -> new BigDecimal(rectangle._1._2.toString),
          'locLonMin -> new BigDecimal(rectangle._2._2.toString)).as(trainPoint *)
    }
  }

}