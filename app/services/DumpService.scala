package services

import akka.actor.ActorSystem
import com.google.inject.Inject

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class DumpService @Inject() (actorSystem: ActorSystem)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 1.minute) {

  }
}
