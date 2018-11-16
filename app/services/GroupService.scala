package services

import com.google.inject.{Inject, Singleton}
import controllers.vo.GroupRequest
import dao.GroupDao
import model.Group
import services.client.VkClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupService @Inject()(groupDao: GroupDao, vkClient: VkClient)(implicit ec: ExecutionContext) {
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
        groupDao.updateGroup(withNewAlias, groupRequest.userIds)
      case _ => vkClient.loadGroup(groupId) match {
          case Some(group) =>
            val withNewAlias = group.withAlias(groupRequest.alias.getOrElse(group.alias))
            groupDao.insertGroup(withNewAlias, groupRequest.userIds).map(_ => withNewAlias)
          case _ => throw new IllegalArgumentException(s"Group with id '$groupId' not found")
        }
    }
  }
}
