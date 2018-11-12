package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.impl.providers.oauth2.VKProvider
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import security.VkSSOEnv

import scala.concurrent.ExecutionContext

class SignInController @Inject() (controllerComponents: ControllerComponents,
                                  silhouette: Silhouette[VkSSOEnv],
                                  vKProvider: VKProvider)(implicit ex: ExecutionContext) extends AbstractController(controllerComponents) {
  def login = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    vKProvider.authenticate().map {
      case Left(result) => result
      case Right(info) => Ok(info.accessToken)
    }.recover {
      case e: ProviderException => InternalServerError(s"Failed to authenticate: ${e.getMessage}")
    }
  }
}
