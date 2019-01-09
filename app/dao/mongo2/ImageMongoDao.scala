package dao.mongo2

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

import com.google.inject.Inject
import dao.ImageDao
import model.{Image, ImageType}
import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonDocument, BsonInt32, BsonInt64, BsonObjectId, BsonString}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.concurrent.Future

class ImageMongoDao @Inject()(configuration: Configuration) extends ImageDao {
  private val pattern = ".*\\/(\\w.*)$".r

  private val uri = configuration.get[String]("mongodb.uri")
  private val client = MongoClient(uri)
  private val database = uri match {
    case pattern(dbName) => client.getDatabase(dbName)
  }
  private val images = database.getCollection("image")

  override def findImagesByGroup(groupId: Long, limit: Int,
                                 offset: Int): Future[Seq[Image]] =
    images
     .find(equal("groupId", groupId))
     .skip(offset)
     .limit(limit)
     .map(readImage).toFuture()

  override def insertImages(imgs: Seq[Image]): Future[_] = images.insertMany(imgs.map(writeImage)).toFuture()

  override def getLastImage(groupId: Long): Future[Option[Image]] = images
    .find(equal("groupId", groupId))
    .sort(Sorts.descending("_id"))
    .map(readImage)
    .headOption()

  /**
    * For use in tests ONLY
    */
  override def deleteAll(): Future[_] = images.deleteMany(Document()).toFuture()

  private def writeLocalDataTime(dateTime: LocalDateTime): BsonInt64 =
    BsonInt64(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli)

  private def readLocalDateTime(instant: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(instant), ZoneId.of("UTC"))

  private def writeImage(image: Image): Document = {
    Document("id" -> image.id,
             "postId" -> image.postId,
             "urls" -> Document(image.urls.map({case (k, v) => (k.toString, v)})),
             "thumbnail" -> image.thumbnail,
             "createdDate" -> writeLocalDataTime(image.createdDate),
             "groupId" -> image.groupId,
             "imageType" -> image.imageType.id,
             "gif" -> image.gif)
  }

  def readImage(document: Document): Image = {
    val urls = document.get[BsonDocument]("urls")
      .map(d => d.entrySet().asScala.map(e => (e.getKey.toInt, e.getValue.asString().getValue)).toMap)
      .getOrElse(Map.empty)

    val gif = document.get[BsonString]("gif").map(_.getValue)

    val imageOpt = for {
        id <- document.get[BsonObjectId]("_id")
        postId <- document.get[BsonInt64]("postId")
        thumbnail <- document.get[BsonString]("thumbnail")
        createdDate <- document.get[BsonInt64]("createdDate")
        groupId <- document.get[BsonInt64]("groupId")
        imageType <- document.get[BsonInt32]("imageType")
      } yield Image(postId.getValue, urls, thumbnail.getValue, readLocalDateTime(createdDate.getValue),
                    groupId.getValue, Some(id.getValue.hashCode), ImageType(imageType.getValue), gif)

    imageOpt.get
  }
}
