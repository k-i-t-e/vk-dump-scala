package dao

import java.time.LocalDateTime

import model.{Group, Image}
import play.api.test.WithApplication

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ImageDaoTest extends AbstractDaoTest {
  "ImageDao" should {
    "Image" in {
      "Created and Loaded" in new WithApplication(appWithInMemoryDatabase) {
        val imageDao = app.injector.instanceOf[ImageDao]
        val groupDao = app.injector.instanceOf[GroupDao]

        val group = Group(1, "test", "test", "test", true, None)
        Await.ready(groupDao.insertGroup(group, Seq.empty), Duration.Inf)

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

  override def afterAll(): Unit = {
    new WithApplication(appWithInMemoryDatabase) {
      val imageDao = app.injector.instanceOf[ImageDao]
      val groupDao = app.injector.instanceOf[GroupDao]

      Await.ready(imageDao.deleteAll(), Duration.Inf)
      Await.ready(groupDao.deleteAll(), Duration.Inf)
    }
  }
}