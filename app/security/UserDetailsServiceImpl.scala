package security

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import model.VkUser

import scala.concurrent.Future

class UserDetailsServiceImpl extends UserDetailsService {
  override def retrieve(loginInfo: LoginInfo): Future[Option[VkUser]] =
    Future.successful(Some(new VkUser(loginInfo.providerKey.toLong, null, null)))
}
