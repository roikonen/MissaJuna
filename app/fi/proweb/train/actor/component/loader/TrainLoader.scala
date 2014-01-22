package fi.proweb.train.actor.component.loader

import fi.proweb.train.actor.component.DataLoader
import akka.actor.ActorRef
import akka.actor.Props
import fi.proweb.train.actor.component.validator.TrainDataValidator
import fi.proweb.train.actor.component.formatter.TrainFormatter
import fi.proweb.train.model.app.Train

object TrainLoader {
  def props(trainGuid: String): Props = Props(new TrainLoader("http://188.117.35.14/TrainRSS/TrainService.svc/trainInfo?train=" + trainGuid))
}

class TrainLoader(url: String) extends DataLoader[Train](Props[TrainDataValidator], Props[TrainFormatter], url)