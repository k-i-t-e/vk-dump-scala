package dao.jdbc.table

import slick.jdbc.PostgresProfile.api._
import slick.lifted

class UserGroupTable(tag: lifted.Tag) extends Table[(Long, Long)](tag, "vk_user_group") {
  def userId = column[Long]("user_id")
  def groupId = column[Long]("group_id")
  override def * = (userId, groupId)
}
