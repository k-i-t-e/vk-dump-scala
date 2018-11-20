package dao.table

import java.time.LocalDateTime

import model.Image
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class ImageTable(tag: Tag) extends Table[Image](tag, "image") {
  import Converters._

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def groupId = column[Long]("group_id")
  def urls = column[Map[Int, String]]("urls")
  def thumbnail = column[String]("thumbnail")
  def postId  = column[Long]("post_id")
  def createdDate = column[LocalDateTime]("created_date")

  override def * = (postId, urls, thumbnail, createdDate, groupId, id.?) <> (Image.tupled, Image.unapply)
}
