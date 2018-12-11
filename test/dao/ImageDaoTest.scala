package dao

import java.time.LocalDateTime

import model.{Group, Image, ImageType}
import org.specs2.mock.Mockito
import org.specs2.specification.AfterAll
import play.api.Application
import play.api.inject.Module
import play.api.test.{PlaySpecification, WithApplication}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class ImageDaoTest extends Module with PlaySpecification with Mockito with AfterAll {
  protected def appWithTestDatabase: Application
  protected def checkImageDaoType: ImageDao => Unit
  protected def checkGroupDaoType: GroupDao => Unit
  protected def daoName: String

  daoName should {
    "Image" in {
      "Created and Loaded" in new WithApplication(appWithTestDatabase) {
        val imageDao = app.injector.instanceOf[ImageDao]
        checkImageDaoType(imageDao)
        val groupDao = app.injector.instanceOf[GroupDao]
        checkGroupDaoType(groupDao)

        val group = Group(1, "test", "test", "test", true, None)
        Await.ready(groupDao.insertGroup(group, Seq.empty), Duration.Inf)

        val urls = Map(1 -> "test1", 2 -> "test2", 3 -> "test3")
        val images = Seq(
          Image(1, urls, "thumbnail", LocalDateTime.now(), group.id, None),
          Image(1, urls, "thumbnail2", LocalDateTime.now(), group.id, None)
        )
        Await.result(imageDao.insertImages(images), Duration.Inf)

        val loaded = Await.result(imageDao.findImagesByGroup(group.id, 10, 0), Duration.Inf)
        loaded.size must beEqualTo(images.size)

        val loadedImage = loaded.head
        loadedImage.urls must beEqualTo(urls)
        loadedImage.imageType shouldEqual ImageType.Image

        val lastImage = Await.result(imageDao.getLastImage(group.id), Duration.Inf)
        lastImage must beSome
        lastImage.get must beEqualTo(loaded.head)
      }
    }
  }

  override def afterAll(): Unit = {
    new WithApplication(appWithTestDatabase) {
      val imageDao = app.injector.instanceOf[ImageDao]
      val groupDao = app.injector.instanceOf[GroupDao]

      Await.ready(imageDao.deleteAll(), Duration.Inf)
      Await.ready(groupDao.deleteAll(), Duration.Inf)
    }
  }
}
