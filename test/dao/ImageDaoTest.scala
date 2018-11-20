package dao

import java.time.LocalDateTime

import com.google.inject.AbstractModule
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.api.actions.{SecuredAction, UnsecuredAction, UserAwareAction}
import controllers.SignInController
import model.{Group, Image}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.{PlaySpecification, WithApplication}
import security.{SilhouetteModule, UserDetailsService, UserDetailsServiceImpl, VkSSOEnv}
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.codingwell.scalaguice.ScalaModule
import org.specs2.mock.Mockito
import play.api
import play.api.Configuration
import play.api.inject.{Binding, Module}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ImageDaoTest extends Module with PlaySpecification with Mockito {
  private val inMemoryDatabaseConfiguration: Map[String, Any] = Map(
    "slick.dbs.default.profile"     -> "slick.jdbc.PostgresProfile$",
    //"slick.dbs.default.driver" -> "slick.driver.H2Driver$",
    "slick.default.db.dataSourceClass" -> "slick.jdbc.DatabaseUrlDataSource",
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.db.properties.url"      -> "jdbc:h2:mem:play;DB_CLOSE_DELAY=-1;MODE=POSTGRESQL;DATABASE_TO_UPPER=FALSE;INIT=create schema if not exists public",
    "slick.dbs.default.db.properties.user"     -> "test",
    "slick.dbs.default.db.properties.password" -> ""
  )

  val mock: SignInController = mock[SignInController]

  def appWithInMemoryDatabase = GuiceApplicationBuilder()
    .disable[SilhouetteModule]
    .overrides(bind[SignInController].toInstance(mock))
    .bindings(bind[UserDetailsService].to[UserDetailsServiceImpl])
    .configure(inMemoryDatabaseConfiguration).build()

  "ImageDao" should {
    "Image" in {
      "Created and Loaded" in new WithApplication(appWithInMemoryDatabase) {
        val imageDao = app.injector.instanceOf[ImageDao]
        val groupDao = app.injector.instanceOf[GroupDao]

        val group = Group(1, "test", "test", "test", true, None)
        groupDao.insertGroup(group, Seq.empty)

        val urls = Map(1 -> "test1", 2 -> "test2", 3 -> "test3")
        val image = Image(1, urls, "thumbnail", LocalDateTime.now(), group.id, None)
        Await.result(imageDao.insertImages(Seq(image)), Duration.Inf)

        val loaded = Await.result(imageDao.findImagesByGroup(group.id, 10, 0), Duration.Inf)
        loaded.size must beEqualTo(1)

        val loadedImage = loaded.head
        loadedImage.urls must beEqualTo(urls)
      }
    }
  }

  override def bindings(environment: api.Environment, configuration: Configuration): Seq[Binding[_]] = ???
}
