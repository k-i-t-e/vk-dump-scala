package dao

import model.{Group, VkUser}
import org.specs2.matcher.ValueCheck
import org.specs2.mock.Mockito
import org.specs2.specification.AfterAll
import play.api.Application
import play.api.inject.Module
import play.api.test.{PlaySpecification, WithApplication}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class UserDaoTest extends Module with PlaySpecification with Mockito with AfterAll {
  protected def appWithTestDatabase: Application
  protected def checkUserDaoType: UserDao => Unit
  protected def checkGroupDaoType: GroupDao => Unit
  protected def daoName: String

  daoName should {
    "User" in {
      "Created and Loaded" in new WithApplication(appWithTestDatabase) {
        val userDao = app.injector.instanceOf[UserDao]
        checkUserDaoType(userDao)
        val groupDao = app.injector.instanceOf[GroupDao]
        checkGroupDaoType(groupDao)

        val testUser = VkUser(1L, Some("Test"), Some("User"), None, None)
        Await.ready(userDao.add(testUser), Duration.Inf)

        val loaded = Await.result(userDao.find(testUser.id), Duration.Inf)
        loaded.get must beEqualTo(testUser)

        val updated = VkUser(1L, Some("User"), Some("Test"), None, None)
        Await.ready(userDao.update(updated), Duration.Inf)

        val updatedLoaded = Await.result(userDao.find(updated.id), Duration.Inf)
        updatedLoaded.get must beEqualTo(updated)

        val testUser2 = VkUser(2L, Some("User2"), Some("Test"), None, None)
        Await.ready(userDao.add(testUser2), Duration.Inf)

        val group = Group(1, "test1", "testName", "testAlias", true, Some(1000), Some(Seq(testUser, testUser2)))
        Await.ready(groupDao.insertGroup(group, Seq(testUser.id, testUser2.id)), Duration.Inf)

        val usersByDomain = Await.result(userDao.findUsersWithGroup(group.domain), Duration.Inf)
        usersByDomain must containAllOf(Seq(updated, testUser2))

        val usersByGroupId = Await.result(userDao.findUsersWithGroup(group.id), Duration.Inf)
        usersByGroupId must containAllOf(Seq(updated, testUser2))

        Await.ready(userDao.updateLastAccess(testUser.id), Duration.Inf)
        val loadedWithLastAccessed = Await.result(userDao.find(testUser.id), Duration.Inf)
        loadedWithLastAccessed.get must beLike { case VkUser(_, _, _, _, lastAccessed) => lastAccessed must beSome }
      }
    }
  }

  override def afterAll(): Unit = {
    new WithApplication(appWithTestDatabase) {
      val groupDao = app.injector.instanceOf[GroupDao]
      val userDao = app.injector.instanceOf[UserDao]
      Await.ready(groupDao.deleteAll(), Duration.Inf)
      Await.ready(userDao.deleteAll(), Duration.Inf)
    }
  }
}
