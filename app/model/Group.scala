package model

case class Group(id: Long, domain: String, name: String, alias: String, fetched: Boolean, offset: Option[Int]) {}
