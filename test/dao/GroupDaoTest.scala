package dao

import model.{Group, VkUser}
import org.specs2.mock.Mockito
import org.specs2.specification.{AfterAll, AfterEach}
import play.api.Application
import play.api.inject.Module
import play.api.test.{PlaySpecification, WithApplication}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

abstract class GroupDaoTest extends Module with PlaySpecification with Mockito with AfterEach with AfterAll {
  protected def appWithTestDatabase: Application
  protected def checkUserDaoType: UserDao => Unit
  protected def checkGroupDaoType: GroupDao => Unit
  protected def daoName: String

  sequential

  daoName should {
    "Group" in  {
      "Created and Loaded" in new WithApplication(appWithTestDatabase) {
        val groupDao = app.injector.instanceOf[GroupDao]
        checkGroupDaoType(groupDao)
        val userDao = app.injector.instanceOf[UserDao]
        checkUserDaoType(userDao)

        val testUser = VkUser(1L, Some("Test"), Some("User"), None, None)
        sync(userDao.add(testUser))

        val group = Group(1, "test1", "testName", "testAlias", true, Some(1000), Some(Seq(testUser)))
        Await.ready(groupDao.insertGroup(group, Seq(testUser.id)), Duration.Inf)

        val groups = Await.result(groupDao.findAll(), Duration.Inf)
        groups must not(beEmpty)
        assertEquals(group, groups.find(_.id == group.id).get)

        val loadedById = Await.result(groupDao.findById(group.id), Duration.Inf)
        assertEquals(group, loadedById.get)

        val loadedByDomain = Await.result(groupDao.findByDomain(group.domain), Duration.Inf)
        assertEquals(group, loadedByDomain.get)

        val groupsWithUsers = sync(groupDao.findAllWithUsers())
        assertEquals(group, groupsWithUsers.head)
        groupsWithUsers.head.users.get must not(beEmpty)
        assertEquals(testUser, groupsWithUsers.head.users.get.head)

        val loadedWithUsers = sync(groupDao.findWithUsers(group.id))
        assertEquals(group, loadedWithUsers.get)
        assertEquals(testUser, loadedWithUsers.get.users.get.head)

        val loadedByUser = sync(groupDao.findGroupsByUser(testUser.id))
        assertEquals(group, loadedByUser.head)
      }
      "Updated" in new WithApplication(appWithTestDatabase) {
        val groupDao = app.injector.instanceOf[GroupDao]
        checkGroupDaoType(groupDao)
        val userDao = app.injector.instanceOf[UserDao]
        checkUserDaoType(userDao)

        val testUser = VkUser(2L, Some("Test"), Some("User"), None, None)
        sync(userDao.add(testUser))

        val group = Group(2, "test2", "testName", "testAlias", true, Some(1000), Some(Seq(testUser)))
        sync(groupDao.insertGroup(group, Seq(testUser.id)))

        val updated1 = group
          .withAlias("XXX")
          .withFetched(false)
          .withOffset(Some(123))

        val user2 = VkUser(3L, Some("User2"), Some("Test2"), None, None)
        sync(userDao.add(user2))
        sync(groupDao.updateGroup(updated1))

        val loaded1 = sync(groupDao.findWithUsers(group.id))
        assertEquals(updated1, loaded1.get)
        assertEquals(testUser, loaded1.get.users.get.head)

        val updated2 = updated1.withAlias("YYY")
        sync { groupDao.updateGroup(updated2, Seq(user2.id)) }

        val loaded2 = sync { groupDao.findWithUsers(updated2.id) }
        assertEquals(updated2, loaded2.get)
        loaded2.get.users.get.size must beEqualTo(1)
        assertEquals(user2, loaded2.get.users.get.head)
      }
    }
  }

  def assertEquals(group: Group, loadedGroup: Group) = {
    val originValues = group.productIterator.filterNot(_.isInstanceOf[Option[Seq[VkUser]]]).toSet
    val loadedValues = loadedGroup.productIterator.filterNot(_.isInstanceOf[Option[Seq[VkUser]]]).toSet
    originValues.diff(loadedValues) must beEmpty
  }

  def assertEquals(user: VkUser, loadedUser: VkUser) = {
    val originValues = user.productIterator.toSet
    val loadedValues = loadedUser.productIterator.toSet
    originValues.diff(loadedValues) must beEmpty
  }

  def sync[R](futureResult: Future[R]) = Await.result(futureResult, Duration.Inf)

  override def after(): Unit = {}

  override def afterAll(): Unit = {
    new WithApplication(appWithTestDatabase) {
      val groupDao = app.injector.instanceOf[GroupDao]
      val userDao = app.injector.instanceOf[UserDao]
      Await.ready(groupDao.deleteAll(), Duration.Inf)
      sync { userDao.deleteAll() }
    }
  }
}
