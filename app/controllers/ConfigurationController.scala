package controllers

import com.google.inject.Inject
import controllers.vo.GroupRequest
import model.{Group, VkUser}
import play.api.libs.json.{JsError, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.GroupService

import scala.concurrent.ExecutionContext

class ConfigurationController @Inject()(cc: ControllerComponents, groupService: GroupService)
                                       (implicit ec: ExecutionContext) extends AbstractController(cc) {
  implicit val groupRequestReads = Json.reads[GroupRequest]
  implicit val userWrites = Json.writes[VkUser]
  implicit val groupWrites = Json.writes[Group]

  private def validateGroupRequest = parse.json
    .validate(v => v.validate.asEither.left.map(e => BadRequest(JsError.toJson(e))))

  def registerGroup = Action.async(validateGroupRequest) {
    request => {
      val groupRequest = request.body
      groupService.registerGroup(groupRequest).map(g => Ok(Json.toJson(g)))
    }
  }

  def addGroupUsers(groupId: Long, userIds: Seq[Long]) = Action.async {
    _ => groupService.addGroupUsers(groupId, userIds).map(g => Ok(Json.toJson(g)))
  }
}
