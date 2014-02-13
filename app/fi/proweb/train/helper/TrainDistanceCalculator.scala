package fi.proweb.train.helper

import fi.proweb.train.model.app.Train

object TrainDistanceCalculator {

  val SPEED_KM_H = 150
  val SUBSTRACT_DISTANCE_IN_M = 500
  
  def countDistance(train: Train, location: (Double, Double)): Int = {
    val substraction = (SUBSTRACT_DISTANCE_IN_M / SPEED_KM_H) * train.speed.get
    val distance = DistanceCalculator.countDistance(train.location.get._1, train.location.get._2, location._1, location._2)-substraction
    if (distance < 0) 0
    else distance
  }
  
  def countDistance(train1: Train, train2: Train): Int = {
    countDistance(train1, (train2.location.get._1, train2.location.get._2))
  }
  
}