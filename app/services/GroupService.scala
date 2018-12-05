package services

import com.google.inject.{Inject, Singleton}
import controllers.vo.GroupRequest
import dao.jdbc.GroupDao
import model.Group

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupService @Inject()(groupDao: GroupDao, vkClientService: VkClientService)(implicit ec: ExecutionContext) {
  def loadGroupsWithUsers: Future[Seq[Group]] = groupDao.findAllWithUsers()
  def loadGroups: Future[Seq[Group]] = groupDao.findAll()
  def loadGroup(domain: String): Future[Option[Group]] = groupDao.findByDomain(domain)

  def registerGroup(groupRequest: GroupRequest): Future[Group] = {
    groupRequest.domain match {
      case Some(d) => groupDao.findByDomain(d).flatMap(group => saveGroup(group, groupRequest, d))
      case _ => groupRequest.id match {
        case Some(id) => groupDao.findById(id).flatMap(group => saveGroup(group, groupRequest, id.toString))
        case _ => throw new IllegalArgumentException("Group request should contain id or domain")
      }
    }
  }

  private def saveGroup(groupOpt: Option[Group], groupRequest: GroupRequest, groupId: String) = {
    groupOpt match {
      case Some(group) =>
        val withNewAlias = group.withAlias(groupRequest.alias.getOrElse(group.alias))
        groupDao.updateGroup(withNewAlias, groupRequest.userIds).map(_ => withNewAlias)
      case _ =>
        val client = groupRequest.userIds.headOption match {
          case Some(userId) => vkClientService.getClient(userId)
          case None => throw new IllegalArgumentException(s"Group should contain at least one authorized user")
        }

        client.flatMap(_.loadGroup(groupId) match {
          case Some(group) =>
            val withNewAlias = group.withAlias(groupRequest.alias.getOrElse(group.alias))
            groupDao.insertGroup(withNewAlias, groupRequest.userIds).map(_ => withNewAlias)
          case _ => throw new IllegalArgumentException(s"Group with id '$groupId' not found")
        })
    }
  }

  def addGroupUsers(groupId: Long, userIds: Seq[Long]): Future[Group] = {
    /*groupDao.findWithUsers(groupId).flatMap {
      case Some(group) =>
        val existingUserIds = group.users.getOrElse(Seq.empty).toStream.map(_.id).toSet
        val newIds = userIds.toSet.diff(existingUserIds)
        groupDao.addGroupUsers(groupId, newIds)
          .flatMap(_ => groupDao.findWithUsers(groupId).map(opt => opt.get))

      case None => throw new IllegalArgumentException(s"Group with id '$groupId' not found")
    }*/

    val updatedGroup = for {
      Some(group) <- groupDao.findWithUsers(groupId)
      existingUserIds = group.users.getOrElse(Seq.empty).toStream.map(_.id).toSet
      newIds = userIds.toSet.diff(existingUserIds)
      _ = groupDao.addGroupUsers(groupId, newIds)
      result <- groupDao.findWithUsers(groupId)
    } yield {
      result
    }

    updatedGroup.map {
      case Some(group) => group
      case None => throw new IllegalArgumentException(s"Group with id '$groupId' not found")
    }
  }

  def updateGroup(group: Group): Future[_] = groupDao.updateGroup(group)
}
