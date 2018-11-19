package dao

import com.google.inject.{Inject, Singleton}
import dao.table.ImageTable
import model.Image
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.PostgresProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImageDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider)(ec: ExecutionContext)
  extends HasDatabaseConfigProvider[PostgresProfile] {
  import profile.api._

  private val images = TableQuery[ImageTable]

  def insertImages(imgs: Seq[Image]): Future[_] = db.run(images ++= imgs)

  def getLastImage(groupId: Long): Future[Option[Image]] =
    db.run(images.filter(_.groupId === groupId).sortBy(_.id.desc).result.headOption)

}
