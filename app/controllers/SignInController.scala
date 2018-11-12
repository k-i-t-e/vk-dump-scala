package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.oauth2.VKProvider
import model.VkUser
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import security.{UserDetailsServiceImpl, VkSSOEnv}

import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject()(controllerComponents: ControllerComponents,
                                  silhouette: Silhouette[VkSSOEnv],
                                  vKProvider: VKProvider,
                                  userDetailsServiceImpl: UserDetailsServiceImpl)(implicit ex: ExecutionContext) extends AbstractController(controllerComponents) {
  implicit val userWrites = Json.writes[VkUser]

  def login = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    vKProvider.authenticate().flatMap {
      case Left(result) => Future.successful(result)
      case Right(info) => (for {
        profile <- vKProvider.retrieveProfile(info)
        user <- userDetailsServiceImpl.save(profile, info.accessToken)
      } yield user).map(user => Ok(Json.toJson(user)))
    }.recover {
      case e: ProviderException => InternalServerError(s"Failed to authenticate: ${e.getMessage}")
    }
  }
}
