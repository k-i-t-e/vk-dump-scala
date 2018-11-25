package model

import java.time.LocalDateTime

import model.ImageType.ImageType


case class Image(postId: Long,
                 urls: Map[Int, String],
                 thumbnail: String,
                 createdDate: LocalDateTime,
                 groupId: Long,
                 id: Option[Long],
                 imageType: ImageType = ImageType.Image) {

  def withId(newId: Long) = Image(postId, urls, thumbnail, createdDate, groupId, Some(newId), imageType)
}

object ImageType extends Enumeration {
  type ImageType = Value
  val Image = Value(1)
  val Gif = Value(2)
}