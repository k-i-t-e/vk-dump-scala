package dao.jdbc.table

import model.Group
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

class GroupTable(tag:Tag) extends Table[Group](tag, "vk_group"){
  def id = column[Long]("id", O.PrimaryKey)
  def name = column[String]("name")
  def alias = column[String]("alias")
  def offset = column[Int]("fetched_offset")
  def domain = column[String]("domain")
  def fetched = column[Boolean]("fetched")

  override def * = (id, domain, name,  alias, fetched, offset.?) <> ((applyFromDB _).tupled, unapplyToDB)

  private def applyFromDB(id: Long, domain: String, name: String, alias: String, fetched: Boolean, offset: Option[Int]) = {
    Group(id, domain, name, alias, fetched, offset)
  }

  private def unapplyToDB(group: Group) = {
    Some(group.id, group.domain, group.name, group.alias, group.fetched, group.offset)
  }
}
