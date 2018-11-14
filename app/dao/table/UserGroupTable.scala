package dao.table

import slick.jdbc.PostgresProfile.api._
import slick.lifted

class UserGroupTable(tag: lifted.Tag) extends Table[(Long, Long)](tag, "vk_user_group") {
  def user_id = column[Long]("user_id")
  def group_id = column[Long]("group_id")
  override def * = (user_id, group_id)
}
