package fi.proweb.train.helper

import fi.proweb.train.model.app.Train

object TrainDistanceCalculator {
  
  def countDistance(train: Train, location: (Double, Double)): Int = {
    DistanceCalculator.countDistance(train.location.get._1, train.location.get._2, location._1, location._2)
  }
  
  def countDistance(train1: Train, train2: Train): Int = {
    countDistance(train1, (train2.location.get._1, train2.location.get._2))
  }
  
}