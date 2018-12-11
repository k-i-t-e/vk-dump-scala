package dao.jdbc

import dao.{GroupDao, GroupDaoTest, UserDao}

class GroupSlickDaoTest extends GroupDaoTest with SlickDaoTest {

  override protected def checkUserDaoType: UserDao => Unit = userDao => userDao must beAnInstanceOf[UserSlickDao]

  override protected def checkGroupDaoType: GroupDao => Unit = _ must beAnInstanceOf[GroupSlickDao]

  override protected def daoName: String = "GroupSlickDao"
}
