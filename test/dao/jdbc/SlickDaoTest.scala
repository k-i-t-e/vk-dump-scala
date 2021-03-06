package dao.jdbc

import controllers.SignInController
import dao.{GroupDao, ImageDao, UserDao}
import org.specs2.mock.Mockito
import play.api
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Binding, Module}
import play.api.test.PlaySpecification
import security.{SilhouetteModule, UserDetailsService, UserDetailsServiceImpl}

trait SlickDaoTest extends Module with PlaySpecification with Mockito {
  protected val testDatabaseConfiguration: Map[String, Any] = Map(
    "backend" -> "jdbc",
    "slick.dbs.default.profile"     -> "slick.jdbc.PostgresProfile$",
    "slick.default.db.dataSourceClass" -> "slick.jdbc.DatabaseUrlDataSource",
    "slick.dbs.default.db.driver" -> "org.postgresql.Driver",
    "slick.dbs.default.db.properties.url"      -> "postgres://postgres:postgres@localhost:5432/vk_dump_test",
    "slick.dbs.default.db.properties.user"     -> "postgres",
    "slick.dbs.default.db.properties.password" -> "postgres"
  )

  protected val mock: SignInController = mock[SignInController]

  protected def appWithTestDatabase = GuiceApplicationBuilder()
    .disable[SilhouetteModule]
    .overrides(bind[SignInController].toInstance(mock))
    .bindings(bind[UserDetailsService].to[UserDetailsServiceImpl])
    .configure(testDatabaseConfiguration).build()

  override def bindings(environment: api.Environment, configuration: Configuration): Seq[Binding[_]] = ???

  protected def checkUserDaoType: UserDao => Unit = _ must beAnInstanceOf[UserSlickDao]
  protected def checkGroupDaoType: GroupDao => Unit = _ must beAnInstanceOf[GroupSlickDao]
  protected def checkImageDaoType: ImageDao => Unit = _ must beAnInstanceOf[ImageSlickDao]
}
