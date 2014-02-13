package fi.proweb.train.actor.component.decorator

import fi.proweb.train.actor.component.AppDataDecorator
import fi.proweb.train.model.app.Train
import fi.proweb.train.helper.TrainDistanceCalculator
import akka.actor.Props

object TrainDecorator {  
  def props(locLat: Double, locLon: Double): Props = Props(new TrainDecorator(locLat, locLon))
}

class TrainDecorator(val locLat: Double, val locLon: Double) extends AppDataDecorator[Train] {
  
  override def decorate(train: Train): Train = {   
    train.distance = Some(TrainDistanceCalculator.countDistance(train, (locLat, locLon)))
    train
  }
  

  
}