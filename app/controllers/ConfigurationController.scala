package controllers

import com.google.inject.Inject
import controllers.vo.GroupRequest
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}

class ConfigurationController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {
  implicit val groupRequestReads = Json.reads[GroupRequest]

  private def validateGroupRequest = parse.json
    .validate(v => v.validate.asEither.left.map(e => BadRequest(JsError.toJson(e))))

  def registerGroup = Action.async(validateGroupRequest) {
    request => {
      val groupRequest = request.body
    }
  }
}
