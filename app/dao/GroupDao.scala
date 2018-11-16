package dao

import com.google.inject.{Inject, Singleton}
import dao.table.{GroupTable, UserGroupTable, VkUserTable}
import model.{Group, VkUser}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] {
  import profile.api._

  private val groups = TableQuery[GroupTable]
  private val groupUsers = TableQuery[UserGroupTable]
  private val users = TableQuery[VkUserTable]

  def findById(groupId: Long): Future[Option[Group]] = db.run(groups.filter(_.id === groupId).result.headOption)
  def findByDomain(domain: String): Future[Option[Group]] = db.run(groups.filter(_.domain === domain).result.headOption)

  def findAllWithUsers(): Future[Seq[Group]] = {
    val query = for {
      g <- groups
      gu <- groupUsers if gu.groupId === g.id
      u <- users if u.id === gu.userId
    } yield (g, u)

    db.run(query.result).map(_.foldLeft(Map[Group, Seq[VkUser]]())((acc, pair) => acc()))
  }

  def updateGroup(group: Group, userIds: Seq[Long]): Future[_] = {
    val action1 = groupUsers.filter(_.groupId === group.id).delete
    val action2 = groupUsers ++= userIds.map(userId => (userId, group.id))
    val action3 = groups.filter(_.id === group.id).update(group)

    db.run((action1 andThen action2 andThen action3).transactionally)
  }

  def insertGroup(group: Group, userIds: Seq[Long]): Future[_] = {
    val action1 = groups += group
    val sequence = if (userIds.nonEmpty) action1 andThen (groupUsers ++= userIds.map(userId => (userId, group.id))) else action1

    db.run(sequence.transactionally)
  }

  def addGroupUsers(groupId: Long, userIds: Seq[Long]):Future[_] = {
    if (userIds.isEmpty) {
      Future.successful()
    } else {
      db.run(groupUsers ++= userIds.map((_, groupId)))
    }
  }

  def removeGroupUsers(groupId: Long, userIds: Seq[Long]):Future[_] = {
    if (userIds.isEmpty) {
      Future.successful()
    } else {
      db.run(groupUsers.filter(ug => ug.groupId === groupId && (ug.userId inSet userIds)).delete)
    }
  }

  def findGroupsByUser(userId: Long): Future[Seq[Group]] = {
    db.run {
      {
        for {
          gu <- groupUsers if gu.userId === userId
          g <- groups if g.id === gu.groupId
        } yield g
      }.result
    }
  }
}
