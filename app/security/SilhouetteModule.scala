package security

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus}
import com.mohiva.play.silhouette.crypto.{JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.VKProvider
import com.mohiva.play.silhouette.impl.providers.{DefaultSocialStateHandler, OAuth2Settings}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.ws.WSClient
import play.api.mvc.CookieHeaderEncoding

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {
  override def configure()= {

  }

  /** HTTP layer implementation.
    */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /** Silhouette environment.
    */
  @Provides
  def provideEnvironment(
                          userService: UserDetailsService,
                          authenticatorService: AuthenticatorService[CookieAuthenticator],
                          eventBus: EventBus): Environment[VkSSOEnv] = {

    Environment[VkSSOEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  def provideAuthenticatorService(configuration: Configuration,
                                  signer: Signer,
                                  crypter: Crypter,
                                  cookieHeaderEncoding: CookieHeaderEncoding,
                                  fingerprintGenerator: FingerprintGenerator,
                                  idGenerator: IDGenerator,
                                  clock: Clock) = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)
    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder,
      fingerprintGenerator, idGenerator, clock)
  }

  @Provides
  def provideVkProvider(httpLayer: HTTPLayer, jcaSigner: JcaSigner, configuration: Configuration) = {
    val settings = OAuth2Settings(
      configuration.getOptional[String]("silhouette.vk.authorization.url"),
      configuration.get[String]("silhouette.vk.access.token.url"),
      configuration.getOptional[String]("silhouette.vk.redirect.url"),
      configuration.getOptional[String]("silhouette.vk.api.url"),
      configuration.get[String]("silhouette.vk.client.id"),
      configuration.get[String]("silhouette.vk.client.secret"),
      configuration.getOptional[String]("silhouette.vk.scope")
    )

    new VKProvider(httpLayer, new DefaultSocialStateHandler(Set.empty, jcaSigner), settings)
  }

  @Provides
  def provideSigner(configuration: Configuration) = {
    val settings = JcaSignerSettings(configuration.get[String]("silhouette.authenticator.signer.key"))
    new JcaSigner(settings)
  }
}
