package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.DistanceCalculator
import akka.actor.Props

object TrainDecorator {
  def props(locLat: Double, locLon: Double): Props = Props(new TrainDecorator(locLat, locLon))
}

class TrainDecorator(locLat: Double, locLon: Double) extends AppDataDecorator[Train] {

  override def decorate(train: Train): Train = {
    train.distance = Some(DistanceCalculator.countDistance(train.location.get._1, train.location.get._2, locLat, locLon))
    train
  }
  
}