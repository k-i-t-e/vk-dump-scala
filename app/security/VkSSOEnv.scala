package security

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import model.VkUser

trait VkSSOEnv extends Env {
  override type I = VkUser
  override type A = CookieAuthenticator
}
