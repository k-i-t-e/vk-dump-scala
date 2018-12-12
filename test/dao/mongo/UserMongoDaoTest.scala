package dao.mongo

import dao.UserDaoTest

class UserMongoDaoTest extends UserDaoTest with MongoDaoTest {
  override protected def daoName: String = "UserMongoDao"
}
