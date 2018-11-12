package dao.table

import model.VkUser
import slick.lifted.Tag
import slick.jdbc.PostgresProfile.api._

class VkUserTable(tag: Tag) extends Table[VkUser](tag, ""){
  def id = column[Long]("id", O.PrimaryKey)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def accessToken = column[String]("access_token")

  override def * = (id, firstName.?, firstName.?, accessToken.?) <> (VkUser.tupled, VkUser.unapply)
}
