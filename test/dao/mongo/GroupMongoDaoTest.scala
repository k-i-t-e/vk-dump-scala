package dao.mongo

import dao.AbstractMongoDaoTest
import model.{Group, VkUser}
import play.api.test.WithApplication

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class GroupMongoDaoTest extends AbstractMongoDaoTest {
  val testUser = VkUser(1L, Some("Test"), Some("User"), None, None)

  "GroupDao" should {
    "Group" in {
      "Created and Loaded" in new WithApplication(appWithTestDatabase) {
        val groupDao = app.injector.instanceOf[GroupMongoDao]
        val userDao = app.injector.instanceOf[UserMongoDao]
        val group = Group(1, "test", "testName", "testAlias", true, Some(1000), Some(Seq(testUser)))
        Await.ready(groupDao.insertGroup(group, Seq(testUser.id)), Duration.Inf)
        Await.ready(userDao.add(testUser), Duration.Inf)

        val groups = Await.result(groupDao.findAll(), Duration.Inf)
        groups must not(beEmpty)
        assertEquals(group, groups.head)

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

  override def afterAll(): Unit = {
    new WithApplication(appWithTestDatabase) {
      val groupDao = app.injector.instanceOf[GroupMongoDao]
      Await.ready(groupDao.deleteAll(), Duration.Inf)
    }
  }
}
