package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.TrainDistanceCalculator
import akka.actor.Props
import java.util.Date
import java.text.Format
import java.text.SimpleDateFormat

object TrainDecorator {

  def props(locLat: Double, locLon: Double): Props = Props(new TrainDecorator(locLat, locLon))
}

class TrainDecorator(val locLat: Double, val locLon: Double) extends AppDataDecorator[Train] {

  private val SPEED_KM_H = 150
  private val SUBSTRACT_DISTANCE_IN_M = 500
  
  val formatter: Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  override def decorate(train: Train): Train = {

    addDistance(train)
    addDate(train)

    train
  }

  def addDistance(train: Train) {
    val substraction = (SUBSTRACT_DISTANCE_IN_M / SPEED_KM_H) * train.speed.get

    val distance = TrainDistanceCalculator.countDistance(train, (locLat, locLon)) - substraction
    train.distance = {
      if (distance < 0) Some(0)
      else Some(distance)
    }
  }
  
  def addDate(train: Train) {
    train.timestamp = Some(formatter.format(new Date()))
  }
  
}