package model

import com.mohiva.play.silhouette.api.Identity

case class VkUser(id: Long,
                  firstName: Option[String],
                  lastName: Option[String],
                  accessToken: Option[String]) extends Identity {}
