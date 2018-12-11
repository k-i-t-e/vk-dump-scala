package dao.mongo

import dao.{GroupDao, GroupDaoTest, UserDao}

class GroupMongoDaoTest extends GroupDaoTest with MongoDaoTest {

  override protected def checkUserDaoType: UserDao => Unit = _ must beAnInstanceOf[UserMongoDao]

  override protected def checkGroupDaoType: GroupDao => Unit = _ must beAnInstanceOf[GroupMongoDao]

  override protected def daoName: String = "GroupMongoDao"
}
