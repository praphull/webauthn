package controllers

import com.fasterxml.jackson.databind.ObjectMapper
import controllers.AdminController.{AddUserRequest, HeaderAdminToken}
import models.{ErrorResponse, ServerConfig}
import play.api.Logger
import play.api.libs.json.{Json, Reads}
import play.api.mvc.{AbstractController, ControllerComponents}
import service.fido.FidoService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AdminController @Inject()(cc: ControllerComponents,
                                fidoService: FidoService,
                                serverConfig: ServerConfig)
  extends AbstractController(cc) {
  private val logger = Logger(this.getClass)
  private val objectMapper = new ObjectMapper()

  /*def showCredentials = Action {
    val res = fidoService.getAllCredentials.map { case (id, userId, c) =>
      val cred = Json.parse(objectMapper.writeValueAsString(c.getDescriptor))
      Json.obj("id" -> id, "userId" -> userId, "credential" -> cred)
    }
    val json = Json.toJson(res)
    logger.info(s"showCredentials: $json")
    Ok(json)
  }*/
  def addUser = Action.async(parse.tolerantJson) { request =>
    request.headers.get(HeaderAdminToken) match {
      case Some(token) =>
        if (token.equals(serverConfig.adminToken)) {
          request.body.asOpt[AddUserRequest] match {
            case Some(req) =>
              logger.info(s"Request to add user: ${req.username}")
              fidoService.addUser(req.username, req.password).map { id =>
                logger.info(s"Added user ${req.username} with id $id")
                Ok(Json.obj("userId" -> id))
              }
            case None => Future.successful(Forbidden(ErrorResponse.InvalidAdminAction.json))
          }
        } else {
          Future.successful(Forbidden(ErrorResponse.InvalidAdminAction.json))
        }
      case None => Future.successful(Forbidden(ErrorResponse.InvalidAdminAction.json))
    }
  }
}

object AdminController {
  val HeaderAdminToken = "X-ADMIN-TOKEN"

  case class AddUserRequest(username: String, password: String)

  object AddUserRequest {
    implicit val reads: Reads[AddUserRequest] = Json.reads[AddUserRequest]
  }

}