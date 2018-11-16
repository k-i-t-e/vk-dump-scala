package dao.table

import model.Group
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class GroupTable(tag:Tag) extends Table[Group](tag, "vk_group"){
  def id = column[Long]("id")
  def name = column[String]("name")
  def alias = column[String]("alias")
  def offset = column[Int]("offset")
  def domain = column[String]("domain")
  def fetched = column[Boolean]("fetched")

  override def * = (id, domain, name,  alias, fetched, offset.?) <> (Group.tupled, Group.unapply)
}
