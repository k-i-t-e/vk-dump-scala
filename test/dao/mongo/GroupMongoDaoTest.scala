package dao.mongo

import dao.GroupDaoTest

class GroupMongoDaoTest extends GroupDaoTest with MongoDaoTest {
  override protected def daoName: String = "GroupMongoDao"
}
