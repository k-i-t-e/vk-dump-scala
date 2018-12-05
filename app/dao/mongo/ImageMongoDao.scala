package dao.mongo

import com.google.inject.Inject
import dao.ImageDao
import model.Image
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}

import scala.concurrent.{ExecutionContext, Future}

class ImageMongoDao @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends ImageDao {
  import dao.mongo.Converters._

  private val db = reactiveMongoApi.database
  private def images = db.map(_.collection[BSONCollection]("image"))

  implicit def imageWriter: BSONDocumentWriter[Image] = Macros.writer[Image]
  implicit def imageReader: BSONDocumentReader[Image] = Macros.reader[Image]

  override def findImagesByGroup(groupId: Long, limit: Int, offset: Int): Future[Seq[Image]] = ???

  override def insertImages(imgs: Seq[Image]): Future[_] = images.flatMap(_.insert(ordered = true).many(imgs))

  override def getLastImage(groupId: Long): Future[Option[Image]] = images.flatMap {
   _
     .find(document("groupId" -> groupId), None)
     .sort(document("_id" -> "desc"))
     .one
  }

  override def deleteAll(): Future[_] = images.flatMap(_.delete().one(document()))
}
