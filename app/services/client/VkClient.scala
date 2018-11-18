package services.client

import java.util
import java.util.concurrent.Semaphore
import java.util.{List, Timer, TimerTask}

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.exceptions.{ApiException, ClientException}
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.groups.GroupFull
import com.vk.api.sdk.objects.wall.WallpostAttachmentType
import model.{Group, Image, VkUser}
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
    val (imagesPortion, totalCount, postsCount) = loadImagePortion(groupId, userActor,
                                 Math.min(MAX_ALLOWED_POSTS_COUNT, limit.getOrElse(MAX_ALLOWED_POSTS_COUNT)), offset)
    val realLimit = limit match {
      case Some(l) => l
      case None => totalCount
    }

    if (realLimit <= postsCount) {
      imagesPortion
    } else {
      imagesPortion ++ (1 until Math.ceil(realLimit.toDouble / MAX_ALLOWED_POSTS_COUNT).toInt)
        .flatMap(i => loadImagePortion(groupId, userActor, calculatePageSize(i, realLimit), MAX_ALLOWED_POSTS_COUNT * i)._1)
    }
  }

  def loadImagesFromBottom(groupId: String, offset: Option[Int] = None): (Seq[Image], Int) = {
    val userActor = new UserActor(user.id.toInt, user.accessToken.get)
    val realOffset = offset match {
      case Some(o) => o
      case None => {
        val (_, totalCount, _) = loadImagePortion(groupId, userActor, 1, 0)
        totalCount - MAX_ALLOWED_POSTS_COUNT
      }
    }

    val pageSize = if (realOffset >= 0) MAX_ALLOWED_POSTS_COUNT else -realOffset
    val (imagesPortion, _, _) = loadImagePortion(groupId, userActor, pageSize, Math.max(realOffset, 0))
    (imagesPortion, Math.max(realOffset, 0))
  }

  def loadGroup(groupId: String): Option[Group] = {
    doRequest {
      val userActor = new UserActor(user.id.toInt, user.accessToken.get)
      val groups = client.groups.getById(userActor).groupId(groupId).execute.asScala
      groups.headOption.map((vkGroup: GroupFull) =>
          Group(vkGroup.getId.longValue, vkGroup.getScreenName, vkGroup.getName, vkGroup.getName, false, None)
      )
    }
  }

  private def calculatePageSize(i: Int, realLimit: Int) = Math.min(MAX_ALLOWED_POSTS_COUNT,
                                                                   Math.abs(MAX_ALLOWED_POSTS_COUNT * i - realLimit))

  private def loadImagePortion(groupId: String, userActor: UserActor, pageSize: Int,
                               offset: Int) = {
    doRequest {
      Logger.debug(s"Loading wall posts portion of size $pageSize, offset $offset")

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

      (images, postsResult.getCount.toInt, postsResult.getItems.size)
    }
  }

  private def doRequest[R](requestFunction: => R): R = {
    requestSemaphore.acquire()
    requestFunction
  }
}
