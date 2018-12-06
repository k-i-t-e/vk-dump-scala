package dao.mongo

import com.google.inject.Inject
import dao.GroupDao
import model.{Group, VkUser}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.{ExecutionContext, Future}

class GroupMongoDao @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends GroupDao {
  private val db = reactiveMongoApi.database
  private def groups = db.map(_.collection[BSONCollection]("vk_group"))
  private def users = db.map(_.collection[BSONCollection]("vk_user"))

  implicit object GroupWriter extends BSONDocumentWriter[Group] {
    override def write(group: Group): BSONDocument = BSONDocument(
        "id" -> group.id,
        "domain" -> group.domain,
        "name" -> group.name,
        "alias" -> group.alias,
        "fetched" -> group.fetched,
        "offset" -> group.offset,
        "users" -> group.users.map(_.map(_.id)))
  }
  implicit def groupReader: BSONDocumentReader[Group] = Macros.reader[Group]

  override def findById(groupId: Long): Future[Option[Group]] = groups.flatMap(_.find(document("id" -> groupId)).one)

  override def findByDomain(domain: String): Future[Option[Group]] = groups.flatMap(_.find(document("domain" -> domain)).one)

  override def findAll(): Future[Seq[Group]] = groups.flatMap {
    _.find(document())
      .cursor[Group]()
      .collect[Seq](-1, Cursor.FailOnError())
  }

  override def findAllWithUsers(): Future[Seq[Group]] =
    for {
      u <- users.flatMap { _.find(document())
                           .cursor[VkUser]()
                           .fold(Map.empty[Long, VkUser])((map, user) => map + (user.id -> user)) }
      g <- groups.flatMap { _.find(document())
                            .cursor[BSONDocument]()
                            .fold(Seq.empty[Group])((res, doc) => {
                              val group = groupReader.read(doc)
                              val withUsers = group.withUsers(doc.getAs[Seq[Long]]("users")
                                                                .map(_.withFilter(u.contains).map(u(_))))
                              res :+ withUsers
                            }) }
    } yield g

  override def findWithUsers(groupId: Long): Future[Option[Group]] =
    for {
      u <- users.flatMap { _.find(document())
        .cursor[VkUser]()
        .fold(Map.empty[Long, VkUser])((map, user) => map + (user.id -> user)) }
      g <- groups.flatMap { _.find(document("id" -> groupId))
        .cursor[BSONDocument]()
        .fold(Seq.empty[Group])((res, doc) => {
          val group = groupReader.read(doc)
          val withUsers = group.withUsers(doc.getAs[Seq[Long]]("users")
                                            .map(_.withFilter(u.contains).map(u(_))))
          res :+ withUsers
        }) }
    } yield g.headOption

  override def updateGroup(group: Group, userIds: Seq[Long]): Future[_] = groups.flatMap {
    _.update(document("id" -> group.id), GroupWriter.write(group) ++ document("users" -> userIds))
  }

  override def updateGroup(group: Group): Future[_] = groups.flatMap {
    _.findAndUpdate(
      document("id" -> group.id),
      document("$set" -> (document(GroupWriter.write(group)) -- "users")))
  }


  override def insertGroup(group: Group, userIds: Seq[Long]): Future[_] = groups.flatMap {
    _.insert(GroupWriter.write(group) ++ document("users" -> userIds))
  }

  override def addGroupUsers(groupId: Long, userIds: Iterable[Long]): Future[_] =
    updateUsers(groupId, oldIds => oldIds ++ userIds)

  override def removeGroupUsers(groupId: Long, userIds: Seq[Long]): Future[_] =
    updateUsers(groupId, oldIds => oldIds -- userIds)


  private def updateUsers(groupId: Long, userIdsUpdate: Set[Long] => Iterable[Long]) =
    for {
      doc <- groups.flatMap(_.find(document("id" -> groupId)).requireOne[BSONDocument])
      newIds = userIdsUpdate(doc.getAs[Seq[Long]]("users").getOrElse(Seq.empty).toSet)
      res <- groups.flatMap(_.update(document("id" -> groupId),
                                     document("$set" -> document("users" -> newIds))))
    } yield res

  override def findGroupsByUser(userId: Long): Future[Seq[Group]] = ???

  override def deleteAll(): Future[Int] = ???
}
