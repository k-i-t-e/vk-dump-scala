package dao

import com.google.inject.Singleton
import dao.table.VkUserTable
import javax.inject.Inject
import model.VkUser
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] { // extends DelegableAuthInfoDAO[OAuth2Info]
  import profile.api._

  private val users = TableQuery[VkUserTable]

  def find(id: Long): Future[Option[VkUser]] = db.run {
    users.filter(u => u.id === id).result.headOption
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

  def save(user: VkUser): Future[VkUser] =
    find(user.id).flatMap {
      case Some(user) => update(user)
      case None => add(user)
    }
}
