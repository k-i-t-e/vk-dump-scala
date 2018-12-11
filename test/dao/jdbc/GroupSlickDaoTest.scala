package dao.jdbc

import dao.GroupDaoTest

class GroupSlickDaoTest extends GroupDaoTest with SlickDaoTest {
  override protected def daoName: String = "GroupSlickDao"
}
