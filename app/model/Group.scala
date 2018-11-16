package model

case class Group(id: Long, domain: String, name: String, alias: String, fetched: Boolean, offset: Option[Int],
                 users: Option[Seq[VkUser]] = None) {
  def withAlias(newAlias: String): Group = {
    Group(id, domain, name, newAlias, fetched, offset)
  }
}
