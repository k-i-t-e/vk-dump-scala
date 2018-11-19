package dao.table

import java.time.LocalDateTime

import model.VkUser
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class VkUserTable(tag: Tag) extends Table[VkUser](tag, "vk_user"){
  import Converters._

  def id = column[Long]("id", O.PrimaryKey)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def accessToken = column[String]("access_token")
  def lastAccessed = column[LocalDateTime]("last_accessed")

  override def * = (id, firstName.?, lastName.?, accessToken.?, lastAccessed.?) <> (VkUser.tupled, VkUser.unapply)
}
