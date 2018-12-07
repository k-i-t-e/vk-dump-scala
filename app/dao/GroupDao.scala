package dao

import model.Group

import scala.concurrent.Future

trait GroupDao {
  def findById(groupId: Long): Future[Option[Group]]
  def findByDomain(domain: String): Future[Option[Group]]
  def findAll(): Future[Seq[Group]]

  def findAllWithUsers(): Future[Seq[Group]]

  def findWithUsers(groupId: Long): Future[Option[Group]]

  def updateGroup(group: Group, userIds: Seq[Long]): Future[_]

  def updateGroup(group: Group): Future[_]

  def insertGroup(group: Group, userIds: Seq[Long]): Future[_]

  def addGroupUsers(groupId: Long, userIds: Iterable[Long]):Future[_]

  def removeGroupUsers(groupId: Long, userIds: Seq[Long]):Future[_]

  def findGroupsByUser(userId: Long): Future[Seq[Group]]

  def deleteAll(): Future[_]
}
