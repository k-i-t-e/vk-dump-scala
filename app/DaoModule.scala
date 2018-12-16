import com.google.inject.{AbstractModule, Inject}
import dao.jdbc.{GroupSlickDao, ImageSlickDao, UserSlickDao}
import dao.mongo.{GroupMongoDao, ImageMongoDao, UserMongoDao}
import dao.{GroupDao, ImageDao, UserDao}
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration

class DaoModule @Inject()(environment: play.api.Environment, configuration: Configuration) extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    configuration.getOptional[String]("backend").getOrElse("jdbc") match {
      case "mongodb" =>
        bind[UserDao].to[UserMongoDao]
        bind[GroupDao].to[GroupMongoDao]
        bind[ImageDao].to[ImageMongoDao]
      case "jdbc" =>
        bind[UserDao].to[UserSlickDao]
        bind[GroupDao].to[GroupSlickDao]
        bind[ImageDao].to[ImageSlickDao]
    }
  }
}
