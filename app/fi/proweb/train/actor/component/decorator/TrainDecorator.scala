package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.DistanceCalculator
import akka.actor.Props

object TrainDecorator {
  
  val SPEED_KM_H = 150
  val SUBSTRACT_DISTANCE_IN_M = 500
  
  def props(locLat: Double, locLon: Double): Props = Props(new TrainDecorator(locLat, locLon))
  
  def countDistance(train: Train, location: (Double, Double)): Int = {
    val substraction = (SUBSTRACT_DISTANCE_IN_M / SPEED_KM_H) * train.speed.get
    val distance = DistanceCalculator.countDistance(train.location.get._1, train.location.get._2, location._1, location._2)-substraction
    if (distance < 0) 0
    else distance
  }
}

class TrainDecorator(val locLat: Double, val locLon: Double) extends AppDataDecorator[Train] {
  
  override def decorate(train: Train): Train = {   
    train.distance = Some(TrainDecorator.countDistance(train, (locLat, locLon)))
    train
  }
  

  
}