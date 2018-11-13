package services

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import model.Image
import play.Logger
import security.UserDetailsService
import services.client.VkClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
class DumpService @Inject()(actorSystem: ActorSystem,
                            userDetailsService: UserDetailsService)(implicit ec: ExecutionContext) {
  private val clientMap = new ConcurrentHashMap[Long, VkClient]()

  def loadImages(userId: Long, groupId: String, count: Int): Future[Seq[Image]] = { // TODO: test method, remove
    val client = Option(clientMap.get(userId)).fold(userDetailsService.load(userId).map(u => {
      val c = new VkClient(u, 60000, 5)
      clientMap.put(userId, c)
      c
    }))(c => Future.successful(c))

    client.map(_.loadImages(groupId, 0, Some(count)))
  }

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 1.minute) {
    Logger.debug("Doing scheduled work")
  }
}
