package controllers

import models.{Constants, UserLoginRequest}
import models.dao.UserDao
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import javax.inject.Inject

class UserController @Inject()(cc: MessagesControllerComponents,
                               userDao: UserDao)
  extends MessagesAbstractController(cc) {

  private val logger = play.api.Logger(this.getClass)

  val form: Form[UserLoginRequest] = Form(
    mapping(
      "username" -> nonEmptyText
        .verifying("too few chars", s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 20)),
      "password" -> nonEmptyText
        .verifying("too few chars", s => lengthIsGreaterThanNCharacters(s, 1))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 30)),
    )(UserLoginRequest.apply)(UserLoginRequest.unapply)
  )

  private val formSubmitUrl = routes.UserController.processLoginAttempt()

  def showLoginForm() = Action { implicit request =>
    Ok(views.html.userLogin(form, formSubmitUrl))
  }

  def processLoginAttempt() = Action { implicit request =>
    val errorFunction = { formWithErrors: Form[UserLoginRequest] =>
      // form validation/binding failed...
      BadRequest(views.html.userLogin(formWithErrors, formSubmitUrl))
    }

    val successFunction = { loginRequest: UserLoginRequest =>
      // form validation/binding succeeded ...
      userDao.findUser(loginRequest.username, loginRequest.password) match {
        case Some(user) =>
          Redirect(routes.LandingPageController.showLandingPage())
            .flashing("info" -> "You are logged in.")
            .withSession(
              Constants.SessionUserIdKey -> user.userId.toString,
              Constants.SessionUsernameKey -> user.username
            )
        case None =>
          Redirect(routes.UserController.showLoginForm())
            .flashing("error" -> "Invalid username/password.")
      }
    }
    val formValidationResult = form.bindFromRequest()
    formValidationResult.fold(
      errorFunction,
      successFunction
    )
  }

  private def lengthIsGreaterThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length > n) true else false
  }

  private def lengthIsLessThanNCharacters(s: String, n: Int): Boolean = {
    if (s.length < n) true else false
  }

}
