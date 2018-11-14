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
    val client = getClient(userId)
    client.map({
       userDetailsService.updateLastAccessed(userId)
       _.loadImages(groupId, 0, Some(count))
     })
  }

  def fetchAllImages(groupId: String, userId: Long): Future[Seq[Image]] = {
    def _fetchAll(images: Seq[Image], client: VkClient, offset: Option[Int]): Seq[Image] = {
      offset match {
        case Some(0) => images // TODO: save to the database, that fetching complete
        case _ =>
          val (i, o) = client.loadImagesFromBottom(groupId, offset)
          // TODO: save offset to the database
          val newImages = images ++ i
          _fetchAll(newImages, client, Some(o))
      }
    }

    getClient(userId).map(c => _fetchAll(Seq.empty, c, None))
  }

  private def getClient(userId: Long) = {
    Option(clientMap.get(userId)).fold(userDetailsService.load(userId).map(u => {
      val c = new VkClient(u, 60000, 5)
      clientMap.put(userId, c)
      c
    }))(c => Future.successful(c))
  }

  actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = 1.minute) {
    Logger.debug("Doing scheduled work")
  }
}
