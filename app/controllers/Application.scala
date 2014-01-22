package controllers

import play.api._
import play.api.mvc._
import global.Global
import akka.pattern.{ ask }
import akka.util.Timeout
import scala.concurrent.duration._
import fi.proweb.train.actor.core.CreateObserver
import fi.proweb.train.actor.core.ObserverCreated
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import fi.proweb.train.actor.core.GetObserversTraintable
import fi.proweb.train.actor.core.Traintable
import fi.proweb.train.model.app.Train
import util.Properties

object Application extends Controller {

  implicit val timeout = Timeout(5 seconds)
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def createObserver = Action.async {
    val locLat = 61.348997
    val locLon = 23.761861
    val future = (Global.trainObserverController ? CreateObserver(locLat, locLon)).mapTo[ObserverCreated]
    future.map(_ match {
      case ObserverCreated(id: Long) => Ok("" + id)
    })
  }
  
  def getTraintable(observerId: Long) = Action.async {
    val future = (Global.trainObserverController ? GetObserversTraintable(observerId)).mapTo[Traintable]
    future.map(_ match {
      case Traintable(traintable: List[Train]) => Ok(traintable mkString(Properties.lineSeparator + Properties.lineSeparator))
    })
  }
   
}