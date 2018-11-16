package dao

import com.google.inject.{Inject, Singleton}
import dao.table.{GroupTable, UserGroupTable}
import model.Group
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] {
  import profile.api._

  private val groups = TableQuery[GroupTable]
  private val groupUsers = TableQuery[UserGroupTable]

  def findById(groupId: Long): Future[Option[Group]] = db.run(groups.filter(_.id === groupId).result.headOption)
  def findByDomain(domain: String): Future[Option[Group]] = db.run(groups.filter(_.domain === domain).result.headOption)

  def updateGroup(group: Group, userIds: Seq[Long]): Future[Group] = {
    val action1 = groupUsers.filter(_.group_id === group.id).delete
    val action2 = groupUsers ++= userIds.map(userId => (userId, group.id))
    val action3 = groups.filter(_.id === group.id).update(group)
    val action4 = groups.filter(_.id === group.id).result.head

    db.run((action1 andThen action2 andThen action3 andThen action4).transactionally)
  }

  def insertGroup(group: Group, userIds: Seq[Long]): Future[_] = {
    val action1 = groups += group
    val sequence = if (userIds.nonEmpty) action1 andThen (groupUsers ++= userIds.map(userId => (userId, group.id))) else action1

    db.run(sequence.transactionally)
  }
}
