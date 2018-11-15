package dao

import com.google.inject.{Inject, Singleton}
import dao.table.GroupTable
import model.Group
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GroupDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] {
  import profile.api._

  private val groups = TableQuery[GroupTable]

  def findById(groupId: Long): Future[Option[Group]] = db.run(groups.filter(_.id === groupId).result.headOption)
  def findByDomain(domain: String): Future[Option[Group]] = db.run(groups.filter(_.domain === domain).result.headOption)
  def updateGroupUsers(groupId: Long, userIds: Seq[Long]): Unit = {

  }
}
