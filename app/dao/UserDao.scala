package dao

import java.time.{Clock, LocalDateTime}

import com.google.inject.Singleton
import dao.table.{GroupTable, UserGroupTable, VkUserTable}
import javax.inject.Inject
import model.VkUser
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] { // extends DelegableAuthInfoDAO[OAuth2Info]
  import profile.api._
  import dao.table.VkUserTable._

  private val users = TableQuery[VkUserTable]
  private val groups = TableQuery[GroupTable]
  private val userGroups = TableQuery[UserGroupTable]

  def find(id: Long): Future[Option[VkUser]] = db.run {
    users.filter(_.id === id).result.headOption
  }

  def add(authInfo: VkUser): Future[VkUser] =
    db.run {
      users += authInfo
    } map {
      _ => authInfo
    }

  def update(authInfo: VkUser): Future[VkUser] =
    db.run {
      val query = for {
        existingUser <- users if existingUser.id === authInfo.id
      } yield existingUser

      query.update(authInfo).transactionally
    } map {
      _ => authInfo
    }

  def findUsersWithGroup(domain: String): Future[Seq[VkUser]] = {
    val query = for {
      g <- groups filter(_.domain === domain)
      ug <- userGroups if g.id === ug.groupId
      u <- users if u.id === ug.userId
    } yield u

    db.run(query.sortBy(_.lastAccessed.desc).result)
  }

  def updateLastAccess(userId: Long): Future[_] = {
    db.run {
       val query = for {
         existingUser <- users if existingUser.id === userId
       } yield existingUser.lastAccessed

       query.update(LocalDateTime.now(Clock.systemUTC())).transactionally
    }
  }

  def save(user: VkUser): Future[VkUser] =
    find(user.id).flatMap {
      case Some(_) => update(user)
      case None => add(user)
    }
}
