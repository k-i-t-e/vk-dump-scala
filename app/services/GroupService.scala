package services

import com.google.inject.{Inject, Singleton}
import controllers.vo.GroupRequest
import dao.GroupDao
import model.Group

@Singleton
class GroupService @Inject()(groupDao: GroupDao) {
  def registerGroup(groupRequest: GroupRequest) = {
    groupRequest.domain match {
      case Some(d) => {
        groupDao.findByDomain(d).flatMap {
           case Some(group) =>
        }
      }
      _ => groupRequest.id match {
        case Some(id) => Group(id, "", groupRequest.name, groupRequest.name, false, None)
        case _ => throw new IllegalArgumentException("Group request should contain id or domain")
      }
    }
  }
}
