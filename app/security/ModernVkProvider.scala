package security

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.{VKProfileParser, VKProvider}
import com.mohiva.play.silhouette.impl.providers.oauth2.VKProvider.ID
import play.api.libs.json.JsValue

import scala.concurrent.Future

class ModernVkProvider(httpLayer: HTTPLayer, socialStateHandler: SocialStateHandler, settings: OAuth2Settings)
  extends VKProvider(httpLayer, socialStateHandler, settings) {
  override val profileParser: VKProfileParser = new ProfileParser()

  class ProfileParser extends VKProfileParser {
    override def parse(json: JsValue, authInfo: OAuth2Info): Future[Profile] = Future.successful {
      val response = (json \ "response").apply(0)
      val userId = (response \ "id").as[Long]
      val firstName = (response \ "first_name").asOpt[String]
      val lastName = (response \ "last_name").asOpt[String]

      CommonSocialProfile(
        loginInfo = LoginInfo(ID, userId.toString),
        firstName = firstName,
        lastName = lastName,
        email = authInfo.params.flatMap(_.get("email")))
    }
  }
}
