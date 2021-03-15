package controllers

import play.api.mvc._

import javax.inject._

@Singleton
class AuthenticatedUserController @Inject()(cc: ControllerComponents,
                                            authenticatedUserAction: AuthenticatedUserAction)
  extends AbstractController(cc) {

  def logout() = authenticatedUserAction { implicit request =>
    // docs: “withNewSession ‘discards the whole (old) session’”
    Redirect(routes.UserController.showLoginForm())
      .flashing("info" -> "You are logged out.")
      .withNewSession
  }

}

