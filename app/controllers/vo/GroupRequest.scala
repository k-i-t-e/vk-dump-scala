package controllers.vo

case class GroupRequest(id: Option[Long], domain: Option[String], alias: Option[String], userIds: Seq[Long])
