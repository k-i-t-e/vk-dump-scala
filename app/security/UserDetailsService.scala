package security

import com.mohiva.play.silhouette.api.services.IdentityService
import model.VkUser

trait UserDetailsService extends IdentityService[VkUser] {

}
