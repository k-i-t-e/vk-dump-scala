package dao.jdbc

import com.google.inject.{Inject, Singleton}
import dao.GroupDao
import dao.jdbc.table.{GroupTable, UserGroupTable, VkUserTable}
import model.Group
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupSlickDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with GroupDao {
  import profile.api._

  private val groups = TableQuery[GroupTable]
  private val groupUsers = TableQuery[UserGroupTable]
  private val users = TableQuery[VkUserTable]

  def findById(groupId: Long): Future[Option[Group]] = db.run(groups.filter(_.id === groupId).result.headOption)
  def findByDomain(domain: String): Future[Option[Group]] = db.run(groups.filter(_.domain === domain).result.headOption)
  def findAll(): Future[Seq[Group]] = db.run(groups.result)
  
  def findAllWithUsers(): Future[Seq[Group]] = {
    val query = for {
      g <- groups
      gu <- groupUsers if gu.groupId === g.id
      u <- users if u.id === gu.userId
    } yield (g, u)

    db.run(query.result)
      .map(
        _.groupBy(_._1)
          .map(p => p._1 -> p._2.map(_._2))
          .foldLeft(Seq[Group]()){ (acc, pair) => acc :+ pair._1.withUsers(Some(pair._2)) }
      )
  }

  def findWithUsers(groupId: Long): Future[Option[Group]] = {
    val query = for {
      g <- groups if g.id === groupId
      gu <- groupUsers if gu.groupId === g.id
      u <- users if u.id === gu.userId
    } yield (g, u)

    db.run(query.result.headOption)
      .map(
        _.groupBy(_._1)
          .map(p => p._1 -> p._2.map(_._2))
          .foldLeft(Seq[Group]()){ (acc, pair) => acc :+ pair._1.withUsers(Some(pair._2.toSeq)) }
          .headOption
      )
  }

  def updateGroup(group: Group, userIds: Seq[Long]): Future[_] = {
    val action1 = groupUsers.filter(_.groupId === group.id).delete
    val action2 = groupUsers ++= userIds.map(userId => (userId, group.id))
    val action3 = groups.filter(_.id === group.id).update(group)

    db.run((action1 andThen action2 andThen action3).transactionally)
  }

  def updateGroup(group: Group): Future[_] = {
    db.run(groups.filter(_.id === group.id).update(group).transactionally)
  }

  def insertGroup(group: Group, userIds: Seq[Long]): Future[_] = {
    val action1 = groups += group
    val sequence = if (userIds.nonEmpty) action1 andThen (groupUsers ++= userIds.map(userId => (userId, group.id))) else action1

    db.run(sequence.transactionally)
  }

  def addGroupUsers(groupId: Long, userIds: Iterable[Long]):Future[_] = {
    if (userIds.isEmpty) {
      Future.successful(None)
    } else {
      db.run(groupUsers ++= userIds.map((_, groupId)))
    }
  }

  def removeGroupUsers(groupId: Long, userIds: Seq[Long]):Future[_] = {
    if (userIds.isEmpty) {
      Future.successful(None)
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

  def deleteAll(): Future[_] = db.run {
    DBIO.seq(groupUsers.delete, groups.delete).transactionally
  }
}
