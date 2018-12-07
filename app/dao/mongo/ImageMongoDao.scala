package dao.mongo

import java.time.LocalDateTime

import com.google.inject.Inject
import dao.ImageDao
import model.{Image, ImageType}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONElement, BSONString, Macros, document}

import scala.concurrent.{ExecutionContext, Future}

class ImageMongoDao @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) extends ImageDao {
  import dao.mongo.Converters._

  private val db = reactiveMongoApi.database
  private def images = db.map(_.collection[BSONCollection]("image"))

  implicit object ImageWriter extends BSONDocumentWriter[Image] {
    override def write(image: Image): BSONDocument =
      BSONDocument(
        "id" -> image.id,
        "postId" -> image.postId,
        "urls" -> BSONDocument(image.urls.map({case (k, v) => BSONElement(k.toString, BSONString(v))})),
        "thumbnail" -> image.thumbnail,
        "createdDate" -> image.createdDate,
        "groupId" -> image.groupId,
        "imageType" -> image.imageType.id,
        "gif" -> image.gif)
  }

  implicit object ImageReader extends BSONDocumentReader[Image] {

    override def read(bson: BSONDocument): Image = {
      val urls = bson.getAs[Map[String, String]]("urls")
        .getOrElse(Map.empty)
        .map({case (k, v) => k.toInt -> v})

      val imageOpt = for {
        id <- bson.getAs[Long]("id")
        postId <- bson.getAs[Long]("postId")
        thumbnail <- bson.getAs[String]("thumbnail")
        createdDate <- bson.getAs[LocalDateTime]("createdDate")
        groupId <- bson.getAs[Long]("groupId")
        imageType <- bson.getAs[Int]("imageType ")
        gif <- bson.getAs[String]("gif")
      } yield Image(postId, urls, thumbnail, createdDate, groupId, Some(id), ImageType(imageType), Some(gif))

      imageOpt.get
    }
  }

  override def findImagesByGroup(groupId: Long, limit: Int, offset: Int): Future[Seq[Image]] = ???

  override def insertImages(imgs: Seq[Image]): Future[_] = images.flatMap(_.insert[Image](ordered = true).many(imgs))

  override def getLastImage(groupId: Long): Future[Option[Image]] = images.flatMap {
   _
     .find(document("groupId" -> groupId))
     .sort(document("_id" -> "desc"))
     .one[Image]
  }

  override def deleteAll(): Future[_] = images.flatMap(_.delete().one(document()))
}
