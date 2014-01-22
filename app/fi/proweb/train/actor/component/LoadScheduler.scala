package fi.proweb.train.actor.component

import akka.actor.Actor
import akka.actor.Cancellable
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.ActorLogging

class LoadScheduler extends Actor with ActorLogging {

  private var scheduler: Option[Cancellable] = None
  private var correntInterval = 0 seconds
  
  def receive = {
    case Stop => stop
    case Start(interval: FiniteDuration) => schedule(0 seconds, interval, sender)
    case Schedule(interval: FiniteDuration) => if (correntInterval != interval) schedule(interval, interval, sender)
  }
  
  def schedule(startIn: FiniteDuration, interval: FiniteDuration, target: ActorRef) {
    scheduler.foreach(_.cancel)
    log.debug("Scheduling " + target + " scheduler to start in " + startIn + " with interval of " + interval)
    scheduler = Some(context.system.scheduler.schedule(startIn, interval, target, Load)(context.system.dispatcher, ActorRef.noSender))
    correntInterval = interval
  }
  
  def stop {
    scheduler.foreach(_.cancel)
    log.debug("Scheduler stopped")
  }
  
}