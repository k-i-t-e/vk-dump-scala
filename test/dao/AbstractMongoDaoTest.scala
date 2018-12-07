package dao

import controllers.SignInController
import org.specs2.mock.Mockito
import org.specs2.specification.AfterAll
import play.api
import play.api.Configuration
import play.api.inject.{Binding, Module}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlaySpecification
import security.{SilhouetteModule, UserDetailsService, UserDetailsServiceImpl}

abstract class AbstractMongoDaoTest extends Module with PlaySpecification with Mockito with AfterAll {
  protected val testDatabaseConfiguration: Map[String, Any] = Map(
    "play.evolutions.db.default.enabled" -> false,
    "backend" -> "mongodb",
    "mongodb.uri" -> "mongodb://localhost:27017/vk_dump_test"
  )

  protected val mock: SignInController = mock[SignInController]

  protected def appWithTestDatabase = GuiceApplicationBuilder()
    .disable[SilhouetteModule]
    .overrides(bind[SignInController].toInstance(mock))
    .bindings(bind[UserDetailsService].to[UserDetailsServiceImpl])
    .configure(testDatabaseConfiguration).build()

  override def bindings(environment: api.Environment, configuration: Configuration): Seq[Binding[_]] = ???
}
