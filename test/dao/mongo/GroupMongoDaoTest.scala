package dao.mongo

import dao.AbstractMongoDaoTest
import model.{Group, VkUser}
import play.api.test.WithApplication

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class GroupMongoDaoTest extends AbstractMongoDaoTest {
  val testUser = VkUser(1L, Some("Test"), Some("User"), None, None)

  "GroupDao" should {
    "Group" in {
      "Created and Loaded" in new WithApplication(appWithTestDatabase) {
        val groupDao = app.injector.instanceOf[GroupMongoDao]
        val group = Group(1, "test", "testName", "teatAlias", true, Some(1000), Some(Seq(testUser)))
        Await.ready(groupDao.insertGroup(group, Seq(testUser.id)), Duration.Inf)

        val users = Await.result(groupDao.findAll(), Duration.Inf)
        users must not(beEmpty)
      }
    }
  }

  override def afterAll(): Unit = {
    new WithApplication(appWithTestDatabase) {
      val groupDao = app.injector.instanceOf[GroupMongoDao]
      Await.ready(groupDao.deleteAll(), Duration.Inf)
    }
  }
}
