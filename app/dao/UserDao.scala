package dao

import model.VkUser

import scala.concurrent.{ExecutionContext, Future}

trait UserDao {
  def find(id: Long): Future[Option[VkUser]]
  def add(authInfo: VkUser): Future[VkUser]
  def update(authInfo: VkUser): Future[VkUser]
  def findUsersWithGroup(domain: String): Future[Seq[VkUser]]
  def findUsersWithGroup(id: Long): Future[Seq[VkUser]]
  def updateLastAccess(userId: Long): Future[_]

  def deleteAll(): Future[_]

  def save(user: VkUser)(implicit ec: ExecutionContext): Future[VkUser] =
    find(user.id).flatMap {
      case Some(_) => update(user)
      case None => add(user)
    }
}
