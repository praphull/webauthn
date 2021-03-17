package controllers

import akka.util.ByteString
import controllers.FidoController.ParamPlatformAuthenticatorOnly
import play.api.http.HttpEntity
import play.api.mvc.{AbstractController, ControllerComponents}
import service.fido.ChallengeGenerator

import javax.inject.{Inject, Singleton}

@Singleton
class FidoController @Inject()(cc: ControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               challengeGenerator: ChallengeGenerator)
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

  def register() = Action { request =>
    Ok("")
  }

  def getLoginChallenge() = Action { request =>
    Ok("")
  }

  def login() = Action { request =>
    Ok("")
  }
}

object FidoController {
  val ParamPlatformAuthenticatorOnly = "platform_only"
}
