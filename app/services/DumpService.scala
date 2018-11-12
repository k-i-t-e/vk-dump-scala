package services

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import com.google.inject.Inject
import model.Image
import play.Logger
import security.UserDetailsService
import services.client.VkClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class DumpService @Inject()(actorSystem: ActorSystem,
                            userDetailsService: UserDetailsService)(implicit executionContext: ExecutionContext) {
  private val clientMap = new ConcurrentHashMap[Long, VkClient]()

  def loadImages(userId: Long, groupId: String): Future[Seq[Image]] = { // TODO: test method, remove
    val client = if (clientMap.containsKey(userId)) {
      Future.successful(clientMap(userId))
    } else {
      userDetailsService.load(userId).map(u => {
        val client = new VkClient(u, 60000, 5)
        clientMap.put(userId, client)
        client
      })
    }

    client.map(_.loadImages(groupId, 0, Some(100)))
  }

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 1.minute) {
    Logger.debug("Doing scheduled work")
  }
}
