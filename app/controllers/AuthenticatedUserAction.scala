package controllers

import play.api.mvc.Results._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedRequest[A](val username: String, request: Request[A])
  extends WrappedRequest[A](request)

/**
 * Cobbled this together from:
 * https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition#Authentication
 * https://www.playframework.com/documentation/2.6.x/api/scala/index.html#play.api.mvc.Results@values
 * `Forbidden`, `Ok`, and others are a type of `Result`.
 */
class AuthenticatedUserAction @Inject()(bodyParser: BodyParsers.Default)
                                       (implicit ec: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest, AnyContent]
    with ActionRefiner[Request, AuthenticatedRequest] {

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    val maybeUsername = request.session.get(models.Global.SESSION_USERNAME_KEY)
    maybeUsername match {
      case None =>
        Future.successful(Left(Forbidden("You’re not logged in!")))
      case Some(u) =>
        Future.successful(Right(new AuthenticatedRequest(u, request)))
    }
  }

  override def parser: BodyParser[AnyContent] = bodyParser

  override protected def executionContext: ExecutionContext = ec
}
