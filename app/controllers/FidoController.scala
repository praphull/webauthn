package controllers

import play.api.mvc.{AbstractController, ControllerComponents}

import javax.inject.{Inject, Singleton}

@Singleton
class FidoController @Inject()(cc: ControllerComponents)
  extends AbstractController(cc) {
  def getRegistrationChallenge() = Action { request =>
    Ok("")
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
