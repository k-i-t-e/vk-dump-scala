package security

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import dao.UserDao
import model.VkUser

import scala.concurrent.Future

class UserDetailsServiceImpl @Inject()(userDao: UserDao) extends UserDetailsService {
  override def retrieve(loginInfo: LoginInfo): Future[Option[VkUser]] =
    Future.successful(Some(new VkUser(loginInfo.providerKey.toLong, null, null, None)))

  override def save(profile: CommonSocialProfile, accessToken: String): Future[VkUser] = {
    val user = VkUser(profile.loginInfo.providerKey.toLong, profile.firstName, profile.lastName, Some(accessToken))
    userDao.save(user)
  }

  override def load(id: Long): Future[VkUser] = userDao.find(id).map(u => u.get)
}
