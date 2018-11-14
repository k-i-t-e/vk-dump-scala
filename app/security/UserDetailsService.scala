package security

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import model.VkUser

import scala.concurrent.Future

trait UserDetailsService extends IdentityService[VkUser] {
  def save(profile: CommonSocialProfile, accessToken: String): Future[VkUser]
  def updateLastAccessed(userId: Long): Future[_]
  def load(id: Long): Future[VkUser]
}
