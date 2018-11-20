package services

import akka.actor.ActorSystem
import com.google.inject.{Inject, Singleton}
import model.{Group, Image, VkUser}
import play.Logger
import play.api.Configuration
import security.UserDetailsService
import services.client.VkClient

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DumpService @Inject()(actorSystem: ActorSystem,
                            userDetailsService: UserDetailsService,
                            groupService: GroupService,
                            vkClientService: VkClientService,
                            imageService: ImageService,
                            configuration: Configuration)(implicit ec: ExecutionContext) {

  val dumpJobEnabled = configuration.getOptional[Boolean]("dump.job.enabled").getOrElse(false)
  val dumpJobPeriod = configuration.getOptional[Int]("dump.job.period").getOrElse(60)
  if (dumpJobEnabled) {
    actorSystem.scheduler.schedule(initialDelay = 0.seconds, interval = dumpJobPeriod.minute) {
      Logger.debug("Doing scheduled work")

      groupService.loadGroups.map(groups => for (group <- groups) yield {
        userDetailsService.findUsersWithGroup(group.id).map(_.headOption).withFilter(_.isDefined).map(user => {
            if (!group.fetched) {
              fetchAllImages(group, user.get, group.offset)
            } else {
              imageService.findLastImage(group.id).map {
                case Some(image) =>
                  val images = vkClientService.getClient(user.get).loadImagesTillPost(group, image.postId)
                  imageService.insertImages(images)
                case None => fetchAllImages(group, user.get, group.offset)
              }
            }
          }
        )
      })
    }
  }

  private def fetchAllImages(group: Group, user: VkUser, initialOffset: Option[Int]): Unit = {
    def _fetchAll(client: VkClient, offset: Option[Int]): Unit = {
      offset match {
        case Some(0) =>
          groupService.updateGroup(group.withOffset(None).withFetched(true))
        case _ =>
          val (i, o) = client.loadImagesFromBottom(group, offset)
          imageService.insertImages(i)
          groupService.updateGroup(group.withOffset(Some(o)))
          _fetchAll(client, Some(o))
      }
    }

    _fetchAll(vkClientService.getClient(user), initialOffset)
  }

  def loadImages(groupId: String, count: Int, offset: Int): Future[Seq[Image]] = { // TODO: test method, remove
    groupService.loadGroup(groupId).flatMap {
      case Some(group) =>
        for {
          user <- userDetailsService.findUsersWithGroup(groupId)
          client <- vkClientService.getClient(user.head.id) if user.nonEmpty
        } yield {
          userDetailsService.updateLastAccessed(user.head.id)
          client.loadImages(group, offset, Some(count))
        }
      case None => throw new IllegalArgumentException(s"No group with ID '$groupId' found")
    }
  }
}
