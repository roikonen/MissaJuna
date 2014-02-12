package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.DistanceCalculator
import akka.actor.Props

object TrainDecorator {
  def props(locLat: Double, locLon: Double): Props = Props(new TrainDecorator(locLat, locLon))
}

class TrainDecorator(locLat: Double, locLon: Double) extends AppDataDecorator[Train] {

  val SPEED_KM_H = 150
  val SUBSTRACT_DISTANCE_IN_M = 500
  
  override def decorate(train: Train): Train = {
    val substraction = (SUBSTRACT_DISTANCE_IN_M / SPEED_KM_H) * train.speed.get
    
    train.distance = Some(DistanceCalculator.countDistance(train.location.get._1, train.location.get._2, locLat, locLon)-substraction)
    train
  }
  
}