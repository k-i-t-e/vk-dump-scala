package dao

import model.Image

import scala.concurrent.Future

trait ImageDao {

  def findImagesByGroup(groupId: Long, limit: Int, offset: Int): Future[Seq[Image]]

  def insertImages(imgs: Seq[Image]): Future[_]

  def getLastImage(groupId: Long): Future[Option[Image]]

  /**
    * For use in tests ONLY
    */
  def deleteAll(): Future[_]
}
