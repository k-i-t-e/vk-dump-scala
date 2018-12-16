package dao.mongo

import java.time._

import com.google.inject.Singleton
import dao.UserDao
import javax.inject.Inject
import model.VkUser
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, document}

import scala.concurrent.{ExecutionContext, Future}

class UserMongoDao @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ec:  ExecutionContext) extends UserDao {

  private val db = reactiveMongoApi.database
  private def users: Future[BSONCollection] = db.map(_.collection[BSONCollection]("vk_user"))
  private def groups: Future[BSONCollection] = db.map(_.collection[BSONCollection]("vk_group"))

  import dao.mongo.Converters._

  override def find(id: Long): Future[Option[VkUser]] = users.flatMap(_.find(document("id" -> id)).one[VkUser])

  override def add(authInfo: VkUser): Future[VkUser] = users.flatMap(_.insert(authInfo).map(_ => authInfo))

  override def update(authInfo: VkUser): Future[VkUser] = users.flatMap {
    _.update(document("id" -> authInfo.id), authInfo).map(_ => authInfo)
  }

  override def findUsersWithGroup(domain: String): Future[Seq[VkUser]] = findByGroups(document("domain" -> domain))

  override def findUsersWithGroup(id: Long): Future[Seq[VkUser]] = findByGroups(document("id" -> id))

  private def findByGroups(doc: BSONDocument) = {
    def findGroupIds = groups.flatMap {
      _.find(doc, Some(document("users" -> 1)))
        .requireOne[BSONDocument]
    }
    def findUsersWithGroupIds(groupUserIds: Seq[Long]) = users.flatMap {
      _.find(document("id" -> document("$in" -> groupUserIds)))
        .cursor[VkUser]()
        .collect[Seq](-1, Cursor.FailOnError[Seq[VkUser]]())
    }

    for {
      groupUserIds <- findGroupIds
      u <- findUsersWithGroupIds(groupUserIds.getAs[Seq[Long]]("users").getOrElse(Seq.empty))
    } yield u
  }

  override def updateLastAccess(userId: Long): Future[_] = users.flatMap {
    _.update(document("id" -> userId),
             document("$set" -> document(
               "lastAccessed" -> LocalDateTime.now(Clock.systemUTC())
             )))
  }

  override def deleteAll(): Future[_] = users.flatMap { _.delete().one(document()) }
}
