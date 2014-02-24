package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.TrainDistanceCalculator
import akka.actor.Props

object TrainDecorator {

  def props(locLat: Double, locLon: Double): Props = Props(new TrainDecorator(locLat, locLon))
}

class TrainDecorator(val locLat: Double, val locLon: Double) extends AppDataDecorator[Train] {

  private val SPEED_KM_H = 150
  private val SUBSTRACT_DISTANCE_IN_M = 500

  override def decorate(train: Train): Train = {

    val substraction = (SUBSTRACT_DISTANCE_IN_M / SPEED_KM_H) * train.speed.get

    val distance = TrainDistanceCalculator.countDistance(train, (locLat, locLon)) - substraction
    train.distance = {
      if (distance < 0) Some(0)
      else Some(distance)
    }

    train
  }

}