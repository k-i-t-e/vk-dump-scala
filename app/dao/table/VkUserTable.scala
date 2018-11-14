package dao.table

import java.sql.Timestamp
import java.time.{LocalDateTime, ZoneOffset}

import model.VkUser
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class VkUserTable(tag: Tag) extends Table[VkUser](tag, "vk_user"){
  import VkUserTable._

  def id = column[Long]("id", O.PrimaryKey)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def accessToken = column[String]("access_token")
  def lastAccessed = column[LocalDateTime]("last_accessed")

  override def * = (id, firstName.?, lastName.?, accessToken.?, lastAccessed.?) <> (VkUser.tupled, VkUser.unapply)
}

object VkUserTable {
  implicit val localDateTimeType: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    dataTime => new Timestamp(dataTime.toInstant(ZoneOffset.of("UTC")).toEpochMilli),
    timestamp => LocalDateTime.ofInstant(timestamp.toInstant, ZoneOffset.of("UTC"))
  )
}
