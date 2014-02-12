package fi.proweb.train.model

abstract class AppData[T <: AppData[T]] {
  def makeCopy: T
}