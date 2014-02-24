package fi.proweb.train.actor.component

import akka.actor.Actor
import akka.actor.Cancellable
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.actor.ActorLogging

class LoadScheduler extends Actor with ActorLogging {

  private val ZERO_DURATION = 0 seconds
  
  private var scheduler: Option[Cancellable] = None
  private var currentInterval = ZERO_DURATION
  
  def receive = {
    case Stop => stop
    case Start(interval: FiniteDuration) => schedule(0 seconds, interval, sender)
    case Schedule(interval: FiniteDuration) => if (currentInterval != interval) schedule(interval, interval, sender)
  }
  
  def schedule(startIn: FiniteDuration, interval: FiniteDuration, target: ActorRef) {
    scheduler.foreach(_.cancel)
    log.debug("Scheduling " + target + " scheduler to start in " + startIn + " with interval of " + interval)
    scheduler = Some(context.system.scheduler.schedule(startIn, interval, target, Load)(context.system.dispatcher, ActorRef.noSender))
    currentInterval = interval
  }
  
  def stop {
    scheduler.foreach(_.cancel)
    currentInterval = ZERO_DURATION
    log.debug("Scheduler stopped")
  }
  
}