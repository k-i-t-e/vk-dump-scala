package dao.jdbc

import dao.ImageDaoTest

class ImageSlickDaoTest extends ImageDaoTest with SlickDaoTest {
  override protected def daoName: String = "ImageSlickDao"
}
