package dao.mongo

import dao.ImageDaoTest

class ImageMongoDaoTest extends ImageDaoTest with MongoDaoTest {
  override protected def daoName: String = "ImageMongoDao"
}
