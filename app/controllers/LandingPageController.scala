package controllers

import play.api.mvc._

import javax.inject._

@Singleton
class LandingPageController @Inject()(cc: ControllerComponents,
                                      authenticatedUserAction: AuthenticatedUserAction)
  extends AbstractController(cc) {

  private val logoutUrl = routes.AuthenticatedUserController.logout()

  // this is where the user comes immediately after logging in.
  // notice that this uses `authenticatedUserAction`.
  def showLandingPage() = authenticatedUserAction { implicit request =>
    Ok(views.html.loginLandingPage(request.username, logoutUrl))
  }

}

