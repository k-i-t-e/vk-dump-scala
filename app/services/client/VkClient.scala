package services.client

import java.util.concurrent.Semaphore
import java.util.{Timer, TimerTask}

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.wall.WallpostAttachmentType
import model.{Image, VkUser}
import play.Logger

import scala.collection.JavaConverters._

class VkClient(user: VkUser, refreshPeriod: Long, maxRequests: Int) {
  private val MAX_ALLOWED_POSTS_COUNT = 100

  private val requestTimer = new Timer(true)
  private val requestSemaphore = new Semaphore(maxRequests)
  private val client: VkApiClient = new VkApiClient(HttpTransportClient.getInstance)

  requestTimer.scheduleAtFixedRate(new TimerTask {
    override def run(): Unit = {
      requestSemaphore.drainPermits
      requestSemaphore.release(maxRequests)
    }
  }, 0, refreshPeriod)

  def loadImages(groupId: String, offset: Int, limit: Option[Int] = None): Seq[Image] = {
    val userActor = new UserActor(user.id.toInt, user.accessToken.get)
    val (imagesPortion, totalCount) = loadImagePortion(groupId, userActor,
                                 Math.min(MAX_ALLOWED_POSTS_COUNT, limit.getOrElse(MAX_ALLOWED_POSTS_COUNT)), offset)
    val realLimit = limit match {
      case Some(l) => l
      case None => totalCount
    }

    if (realLimit <= imagesPortion.size) {
      imagesPortion
    } else {
      imagesPortion ++ (1 to Math.ceil(realLimit / MAX_ALLOWED_POSTS_COUNT).toInt)
        .flatMap(i => loadImagePortion(groupId, userActor, calculatePageSize(i, realLimit), MAX_ALLOWED_POSTS_COUNT * i)._1)
    }
  }

  private def calculatePageSize(i: Int, realLimit: Int) = Math.min(MAX_ALLOWED_POSTS_COUNT, Math.abs(MAX_ALLOWED_POSTS_COUNT * i - realLimit))

  private def loadImagePortion(groupId: String, userActor: UserActor, pageSize: Int,
                               offset: Int) = {
    doRequest {
      Logger.debug("Loading wall posts portion of size {}, offset {}", pageSize, offset)

      val postsResult = client.wall
        .get(userActor)
        .domain(groupId)
        .count(pageSize)
        .offset(offset)
        .execute

      val images = for {
        p <- postsResult.getItems.asScala if p.getAttachments != null
        a <- p.getAttachments.asScala if a.getType == WallpostAttachmentType.PHOTO
      } yield {
        Image(p.getId.toLong, a.getPhoto.getPhoto75, a.getPhoto.getPhoto130, a.getPhoto.getPhoto604,
              a.getPhoto.getPhoto807, a.getPhoto.getPhoto1280, a.getPhoto.getPhoto2560)
      }

      (images, postsResult.getCount.toInt)
    }
  }

  private def doRequest[R](requestFunction: => R): R = {
    requestSemaphore.acquire()
    requestFunction()
  }
}
