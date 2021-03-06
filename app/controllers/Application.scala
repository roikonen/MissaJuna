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
  val DEFAULT_LOCATION = (61.348997, 23.761861)
  val GOOGLE_API_KEY = "AIzaSyAayQ4J_OhlNkbbYZCLxEbWEmod9jRixcM"
  
  implicit val timeout = Timeout(5 seconds)

  def indexDebug = Action.apply {
    val locLat = DEFAULT_LOCATION._1
    val locLon = DEFAULT_LOCATION._2
    Ok(views.html.index(locLat, locLon))
  }
  
  def index = Action.apply {
    Ok(views.html.mobile(GOOGLE_API_KEY, DEFAULT_LOCATION._1, DEFAULT_LOCATION._2))
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
  
  def mobile(googleApiKey: String) = Action.apply {
    Ok(views.html.mobile(googleApiKey, DEFAULT_LOCATION._1, DEFAULT_LOCATION._2))
  }
   
}
