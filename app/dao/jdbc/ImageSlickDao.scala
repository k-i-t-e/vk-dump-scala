package dao.jdbc

import com.google.inject.{Inject, Singleton}
import dao.ImageDao
import dao.jdbc.table.ImageTable
import model.Image
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class ImageSlickDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with ImageDao {
  import profile.api._

  private val images = TableQuery[ImageTable]

  def findImagesByGroup(groupId: Long, limit: Int, offset: Int): Future[Seq[Image]] = db.run {
      images
        .filter(_.groupId === groupId)
        .sortBy(_.id.desc)
        .take(limit)
        .drop(offset)
        .result
    }

  def insertImages(imgs: Seq[Image]): Future[_] = db.run(images ++= imgs)

  def getLastImage(groupId: Long): Future[Option[Image]] =
    db.run { images.filter(_.groupId === groupId).sortBy(_.id.desc).result.headOption }

  def deleteAll(): Future[_] = db.run(images.delete.transactionally)
}
