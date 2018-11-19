package model

import java.time.LocalDateTime

case class Image(postId: Long, urls: Map[Int, String], thumbnail: String, createdDate: LocalDateTime, groupId: Long,
                 id: Option[Long]) {
  def withId(newId: Long) = Image(postId, urls, thumbnail, createdDate, groupId, Some(newId))
}
