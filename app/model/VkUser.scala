package model

import java.time.LocalDateTime

import com.mohiva.play.silhouette.api.Identity

case class VkUser(id: Long,
                  firstName: Option[String],
                  lastName: Option[String],
                  accessToken: Option[String],
                  lastAccessed: Option[LocalDateTime]) extends Identity {}
