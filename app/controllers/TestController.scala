package controllers

import com.google.inject.Inject
import model.Image
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import services.DumpService

import scala.concurrent.ExecutionContext

class TestController @Inject()(cc: ControllerComponents,
                               dumpService: DumpService)(implicit ec: ExecutionContext) extends AbstractController(cc){
  implicit val imageWrites = Json.writes[Image]

  def testGetImages(groupId: String, count: Int, offset: Int) = Action.async {
    _ => dumpService.loadImages(groupId, count, offset).map(images => Ok(Json.toJson(images)))
  }
}
