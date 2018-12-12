package dao.jdbc

import dao.UserDaoTest

class UserSlickDaoTest extends UserDaoTest with SlickDaoTest {
  override protected def daoName: String = "UserSlickDao"
}
