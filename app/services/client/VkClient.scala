package services.client

import java.time.{Instant, ZoneId}
import java.util.concurrent.Semaphore
import java.util.{Timer, TimerTask}

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.groups.GroupFull
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.objects.wall.WallpostAttachmentType
import model.{Group, Image, ImageType, VkUser}
import play.Logger

import scala.collection.JavaConverters._
import scala.util.Try

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

  def loadImages(group: Group, offset: Int, limit: Option[Int] = None): Try[Seq[Image]] = {
    Try {
      val userActor = new UserActor(user.id.toInt, user.accessToken.get)
      val (imagesPortion, totalCount, postsCount) = loadImagePortion(group, userActor,
        Math.min(MAX_ALLOWED_POSTS_COUNT, limit.getOrElse(MAX_ALLOWED_POSTS_COUNT)), offset)
      val realLimit = limit match {
        case Some(l) => l
        case None => totalCount
      }

      if (realLimit <= postsCount) {
        imagesPortion
      } else {
        imagesPortion ++ (1 until Math.ceil(realLimit.toDouble / MAX_ALLOWED_POSTS_COUNT).toInt)
          .flatMap(i => loadImagePortion(group, userActor, calculatePageSize(i, realLimit), MAX_ALLOWED_POSTS_COUNT * i)._1)
      }
    }
  }

  def loadImagesFromBottom(group: Group, offset: Option[Int] = None): (Seq[Image], Int) = {
    val userActor = new UserActor(user.id.toInt, user.accessToken.get)
    val realOffset = offset match {
      case Some(o) => o
      case None =>
        val (_, totalCount, _) = loadImagePortion(group, userActor, 1, 0)
        totalCount - MAX_ALLOWED_POSTS_COUNT
    }

    val pageSize = if (realOffset >= 0) MAX_ALLOWED_POSTS_COUNT else -realOffset
    val (imagesPortion, _, _) = loadImagePortion(group, userActor, pageSize, Math.max(realOffset, 0))
    (imagesPortion, Math.max(realOffset - pageSize, 0))
  }

  def loadImagesTillPost(group: Group, postId: Long): Seq[Image] = {
    val userActor = new UserActor(user.id.toInt, user.accessToken.get)

    def _loadImages(images: Seq[Image], offset: Int): Seq[Image] = {
      val (i, _, newOffset) = loadImagePortion(group, userActor, MAX_ALLOWED_POSTS_COUNT, offset)
      val index = i.indexWhere(_.postId == postId.toInt)
      if (index >= 0) {
        images ++ i.take(index)
      } else {
        _loadImages(images ++ i, newOffset)
      }
    }


    _loadImages(Seq.empty, 0)
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

  private def loadImagePortion(group: Group, userActor: UserActor, pageSize: Int,
                               offset: Int) = {
    doRequest {
      Logger.debug(s"Loading '${group.domain}' wall posts portion of size $pageSize, offset $offset")

      val postsResult = client.wall
        .get(userActor)
        .domain(group.domain)
        .count(pageSize)
        .offset(offset)
        .execute

      val images = for {
        p <- postsResult.getItems.asScala if p.getAttachments != null
        a <- p.getAttachments.asScala if a.getType == WallpostAttachmentType.PHOTO ||
          (a.getType == WallpostAttachmentType.DOC && a.getDoc.getExt == "gif")
      } yield {
        val urls = if (a.getType == WallpostAttachmentType.PHOTO) {
          Map(
            75 -> a.getPhoto.getPhoto75,
            130 -> a.getPhoto.getPhoto130,
            604 -> a.getPhoto.getPhoto604,
            807 -> a.getPhoto.getPhoto807,
            1280 -> a.getPhoto.getPhoto1280,
            2560 -> a.getPhoto.getPhoto2560
          )
        } else {
          a.getDoc.getPreview.getPhoto.getSizes.asScala
            .foldLeft(Map.empty[Int, String])((acc, s) => acc + (s.getWidth.intValue() -> s.getSrc))
        }

        Image(p.getId.toLong, urls, getThumbnail(urls),
              Instant.ofEpochSecond(p.getDate.toLong).atZone(ZoneId.of("UTC")).toLocalDateTime,
              group.id,
              None,
              if (a.getType == WallpostAttachmentType.PHOTO) ImageType.Image else ImageType.Gif,
              if (a.getType == WallpostAttachmentType.DOC) Some(a.getDoc.getUrl) else None)
      }

      (images, postsResult.getCount.toInt, postsResult.getItems.size)
    }
  }

  private def getThumbnail(photo: Photo) = photo.getPhoto604
  private def getThumbnail(urls: Map[Int, String]) = urls.getOrElse(604, urls.filter(_._1 < 604).maxBy(_._1)._2)
  private def doRequest[R](requestFunction: => R): R = {
    requestSemaphore.acquire()
    requestFunction
  }
}
