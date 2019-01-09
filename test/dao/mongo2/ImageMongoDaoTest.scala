package dao.mongo2

import dao.ImageDaoTest

class ImageMongoDaoTest extends ImageDaoTest with MongoDaoTest {
  override protected def daoName: String = "ImageMongoDao2"
}
