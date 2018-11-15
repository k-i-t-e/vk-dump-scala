package controllers.vo

case class GroupRequest(id: Option[Long], domain: Option[String], name: String, userIds: Seq[Long])
