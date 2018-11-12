package security

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.providers.oauth2.VKProvider
import com.mohiva.play.silhouette.impl.providers.{DefaultSocialStateHandler, OAuth2Info, OAuth2Settings}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.mvc.CookieHeaderEncoding

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends AbstractModule with ScalaModule {
  override def configure()= {
    bind[UserDetailsService].to[UserDetailsServiceImpl]
    bind[DelegableAuthInfoDAO[OAuth2Info]].toInstance(new InMemoryAuthInfoDAO[OAuth2Info])

    bind[Silhouette[VkSSOEnv]].to[SilhouetteProvider[VkSSOEnv]]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
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

  @Provides @Named("authenticator-signer")
  def provideSigner(configuration: Configuration): Signer = {
    val settings = JcaSignerSettings(configuration.get[String]("silhouette.authenticator.signer.key"))
    new JcaSigner(settings)
  }

  @Provides
  def provideAuthenticatorService(configuration: Configuration,
                                  @Named("authenticator-signer") signer: Signer,
                                  crypter: Crypter,
                                  cookieHeaderEncoding: CookieHeaderEncoding,
                                  fingerprintGenerator: FingerprintGenerator,
                                  idGenerator: IDGenerator,
                                  clock: Clock): AuthenticatorService[CookieAuthenticator] = {
    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)
    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder,
      fingerprintGenerator, idGenerator, clock)
  }

  @Provides
  def provideVkProvider(httpLayer: HTTPLayer, @Named("social-state-signer") jcaSigner: Signer, configuration: Configuration): VKProvider = {
    val settings = OAuth2Settings(
      configuration.getOptional[String]("silhouette.vk.authorizationURL"),
      configuration.get[String]("silhouette.vk.accessTokenURL"),
      configuration.getOptional[String]("silhouette.vk.redirectURL"),
      configuration.getOptional[String]("silhouette.vk.apiURL"),
      configuration.get[String]("silhouette.vk.clientID"),
      configuration.get[String]("silhouette.vk.clientSecret"),
      configuration.getOptional[String]("silhouette.vk.scope")
    )

    new ModernVkProvider(httpLayer, new DefaultSocialStateHandler(Set.empty, jcaSigner), settings)
  }

  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.socialStateHandler.signer")

    new JcaSigner(config)
  }

  @Provides
  def provideCrypter(configuration: Configuration): Crypter = {
    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }
}
