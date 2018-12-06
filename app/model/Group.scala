package model

import reactivemongo.bson.Macros.Annotations.Ignore

case class Group(id: Long, domain: String, name: String, alias: String, fetched: Boolean, offset: Option[Int],
                 @Ignore users: Option[Seq[VkUser]] = None) {
  def withAlias(newAlias: String): Group = Group(id, domain, name, newAlias, fetched, offset)

  def withUsers(newUsers: Option[Seq[VkUser]]) = Group(id, domain, name, alias, fetched, offset, newUsers)

  def withOffset(newOffset: Option[Int]) = Group(id, domain, name, alias, fetched, newOffset, users)

  def withFetched(newFetched: Boolean) = Group(id, domain, name, alias, newFetched, offset, users)
}
