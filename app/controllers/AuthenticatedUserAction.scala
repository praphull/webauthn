package controllers

import models.dao.UserDao
import models.{Constants, ErrorResponse}
import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthenticatedRequest[A](val userId: Long, val username: String, request: Request[A])
  extends WrappedRequest[A](request)

/**
 * Cobbled this together from:
 * https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition#Authentication
 * https://www.playframework.com/documentation/2.6.x/api/scala/index.html#play.api.mvc.Results@values
 * `Forbidden`, `Ok`, and others are a type of `Result`.
 */
class AuthenticatedUserAction @Inject()(bodyParser: BodyParsers.Default,
                                        userDao: UserDao)
                                       (implicit ec: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest, AnyContent]
    with ActionRefiner[Request, AuthenticatedRequest] {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    val maybeUserid = {
      val maybeFromSession = request.session.get(Constants.SessionUserTokenKey)
      val fromHeadersOrSession = maybeFromSession.fold(
        request.headers.get(Constants.HeaderUserTokenKey)
      )(_ => maybeFromSession)
      //Fixme Remove conversion to toLong when an issued token is used instead of user id
      Try(fromHeadersOrSession.map(_.toLong)).toOption.flatten
    }

    maybeUserid match {
      case Some(userId) =>
        userDao.findById(userId).map {
          case Some(user) =>
            Right(new AuthenticatedRequest(user.userId, user.username, request))
          case None =>
            Left(Forbidden(ErrorResponse.InvalidToken.json))
        }
      case _ =>
        Future.successful(Left(Forbidden(ErrorResponse.NotLoggedIn.json)))
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser

  override protected def executionContext: ExecutionContext = ec
}