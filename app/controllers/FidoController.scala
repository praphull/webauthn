package controllers

import akka.util.ByteString
import controllers.FidoController.ParamPlatformAuthenticatorOnly
import models.{Constants, ErrorResponse}
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import service.fido.{ChallengeGenerator, FidoService}

import javax.inject.{Inject, Singleton}
import scala.util.Try

@Singleton
class FidoController @Inject()(cc: ControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               challengeGenerator: ChallengeGenerator,
                               fidoService: FidoService)
  extends AbstractController(cc) {
  def getRegistrationChallenge() = authenticatedUserAction { implicit request =>

    val restrictPlatformAuthenticatorOnly = request
      .getQueryString(ParamPlatformAuthenticatorOnly).exists(_.toBoolean)

    val options = challengeGenerator.getRegistrationChallenge(
      request.username,
      request.username,
      restrictPlatformAuthenticatorOnly
    )

    Ok.sendEntity(HttpEntity.Strict(ByteString(options), Some("application/json")))
  }

  def getUserId() = Action { request =>
    request.headers.get(Constants.HeaderUsernameKey).flatMap { userName =>
      fidoService.getUserId(userName)
    } match {
      case Some(userId) => Ok(Json.obj("userId" -> userId))
      case None => NotFound(ErrorResponse.InvalidUsername.json)
    }
  }

  def register() = Action { request =>
    Ok("")
  }

  def getLoginChallenge() = Action { request =>
    Try(request.headers.get(Constants.HeaderUserIdKey).map(_.toLong)).toOption.flatten match {
      case None => NotFound(ErrorResponse.InvalidUserId.json)
      case Some(userId) => challengeGenerator.getLoginChallenge(userId) match {
        case None => BadRequest(ErrorResponse.InvalidUserId.json)
        case Some(options) => Ok.sendEntity(HttpEntity.Strict(ByteString(options), Some("application/json")))
      }
    }
  }

  def login() = Action { request =>
    Ok("")
  }
}

object FidoController {
  val ParamPlatformAuthenticatorOnly = "platform_only"
}
