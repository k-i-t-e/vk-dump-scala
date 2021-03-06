package controllers

import com.google.inject.Inject
import model.Image
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.ImageService

import scala.concurrent.ExecutionContext

class ImageController @Inject()(cc: ControllerComponents, imageService: ImageService)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  implicit val imageWrites: Writes[Image] = o => Json.obj(
    "urls" -> o.urls.map(e => (e._1.toString, e._2)),
    "postId" -> o.postId,
    "thumbnail" -> o.thumbnail,
    "groupId" -> o.groupId,
    "id" -> o.id
  )

  def loadImages(groupId: Long, page: Int, pageSize: Int = 50) = Action.async {
    _ => imageService.loadImages(groupId, page, pageSize).map(i => Ok(Json.toJson(i)))
  }
}
