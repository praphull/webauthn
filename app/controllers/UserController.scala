package controllers

import models.dao.UserDao
import models.{Constants, ErrorResponse, User, UserLoginRequest}
import play.api.data.Forms._
import play.api.data._
import play.api.data.format.Formats.stringFormat
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserController @Inject()(cc: MessagesControllerComponents,
                               userDao: UserDao)
  extends MessagesAbstractController(cc) {

  private val logger = play.api.Logger(this.getClass)

  val form: Form[UserLoginRequest] = Form(
    mapping(
      "username" -> of[String],
      "password" -> of[String],
    )(UserLoginRequest.apply)(UserLoginRequest.unapply)
  )

  private val formSubmitUrl = routes.UserController.processLoginForm()

  def showLoginForm() = Action { implicit request =>
    Ok(views.html.userLogin(form, formSubmitUrl))
  }

  private def doLogin[A](loginRequest: UserLoginRequest)
                        (implicit request: Request[A]): Future[Either[String, User]] = {
    import service.Validators._
    validate(List(
      (loginRequest.username -> "username")
        .verifying("too few chars", s => lengthIsGreaterThanNCharacters(s, 2))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 20))
        .result,
      (loginRequest.password -> "password")
        .verifying("too few chars", s => lengthIsGreaterThanNCharacters(s, 1))
        .verifying("too many chars", s => lengthIsLessThanNCharacters(s, 30))
        .result
    )) match {
      case Left((field, error)) =>
        Future.successful(Left(s"Validation failed for '$field' field: $error"))
      case Right(_) =>
        logger.info(s"doLogin: Login attempt: $loginRequest")
        userDao.findUser(loginRequest.username, loginRequest.password).map {
          case Some(user) =>
            logger.info(s"doLogin: User logged in: $user")
            Right(user)
          case None => Left("Invalid username/password.")
        }
    }
  }

  //For API
  def processLoginAttempt() = Action.async(parse.tolerantJson) { implicit request =>
    request.body.asOpt[UserLoginRequest] match {
      case Some(loginRequest) => doLogin(loginRequest).map {
        case Left(error) => BadRequest(Json.toJson(ErrorResponse(4002, error)))
        case Right(user) => Ok(Json.toJson(user))
      }
      case None => Future.successful(BadRequest(ErrorResponse.InvalidLoginRequest.json))
    }
  }

  //For Web UI
  def processLoginForm() = Action.async { implicit request =>
    val errorFunction = { formWithErrors: Form[UserLoginRequest] =>
      Future.successful(BadRequest(views.html.userLogin(formWithErrors, formSubmitUrl)))
    }

    val successFunction = { loginRequest: UserLoginRequest =>
      doLogin(loginRequest).map {
        case Right(user) =>
          Redirect(routes.LandingPageController.showLandingPage())
            .flashing("info" -> "You are logged in.")
            .withSession(
              Constants.SessionUserTokenKey -> user.userId.toString
            )
        case Left(error) =>
          Redirect(routes.UserController.showLoginForm())
            .flashing("error" -> error)
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
