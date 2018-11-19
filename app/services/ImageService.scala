package services

import com.google.inject.{Inject, Singleton}
import dao.ImageDao
import model.Image

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImageService @Inject()(imageDao: ImageDao)(ec: ExecutionContext) {
  def loadImages(groupId: Long, page: Int, pageSize: Int): Future[Seq[Image]] =
    imageDao.findImagesByGroup(groupId, pageSize, page * pageSize)

  def insertImages(images: Seq[Image]): Future[_] = imageDao.insertImages(images)

  def findLastImage(groupId: Long): Future[Option[Image]] = imageDao.getLastImage(groupId)
}
