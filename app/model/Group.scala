package model

case class Group(id: Long, domain: String, name: String, fetched: Boolean, offset: Option[Int]) {}
