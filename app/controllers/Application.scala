package controllers

import play.api._
import play.api.mvc._
import global.Global
import akka.pattern.{ ask }
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import fi.proweb.train.actor.core.GetTraintable
import fi.proweb.train.model.app.Train
import util.Properties
import models.TrainPoint
import play.api.libs.json.Json
import models.Traintable

object Application extends Controller {

  /**
   * The radius from observers geo-location where to search trains to observe
   */
  val OBSERVATION_RADIUS = 1000
  
  implicit val timeout = Timeout(5 seconds)

  def indexDebug = Action.apply {
    val locLat = 61.348997
    val locLon = 23.761861
    Ok(views.html.index(locLat, locLon))
  }
  
  def traintable(locLat: Double, locLon: Double) = Action.async {
    val future = (Global.trainObserverController ? GetTraintable(locLat, locLon)).mapTo[Traintable]
    future.map(_ match {
      case traintable: Traintable => Ok(Json.toJson(traintable))
    })
  }
  
  def traintableDebug(locLat: Double, locLon: Double) = Action.async {
    val future = (Global.trainObserverController ? GetTraintable(locLat, locLon)).mapTo[Traintable]
    future.map(_ match {
      case traintable: Traintable => Ok(views.html.traintable(traintable.toString))
    })
  }
  
  def keepAwake = Action.apply {
    Ok(views.html.keepawake())
  }
  
  def countTrainPoints = Action.apply {
    Ok(TrainPoint.countAll.toString)
  }
  
  def trainsNearBy(locLat: Double, locLon: Double) = Action.apply {
    Ok(TrainPoint.findTrains(locLat, locLon, OBSERVATION_RADIUS).toString)
  }
   
}
