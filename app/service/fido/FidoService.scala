package service.fido

import com.google.inject.{Inject, Singleton}
import models.dao.{ChallengeDao, CredentialsDao, UserDao}
import models.{ChallengeType, Credential, CredentialRegistrationResponse, User}
import service.Util

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class FidoService @Inject()(credentialsDao: CredentialsDao,
                            userDao: UserDao,
                            challengeDao: ChallengeDao) {
  /*def getRegisteredPublicKeys(username: String): Option[List[Credential]] = for {
    user <- userDao.findByUsername(username)
  } yield credentialsDao.getRegisteredPublicKeys(user.userId)*/

  def getRegisteredPublicKeys(userId: Long): Future[Option[List[Credential]]] = for {
    maybeUser <- userDao.findById(userId)
    res <- maybeUser match {
      case Some(user) => credentialsDao.getRegisteredCredentials(user.userId).map(c => Some(c.toList))
      case None => Future.successful(None)
    }
  } yield res

  def getUserId(username: String): Future[Option[Long]] =
    userDao.findByUsername(username).map(_.map(_.userId))

  def registerCredentials(userId: Long,
                          dto: Credential): Future[Either[String, CredentialRegistrationResponse]] =
    credentialsDao.registerCredentials(userId, dto)

  def addChallenge(userId: Long, challengeType: ChallengeType,
                   challenge: Array[Byte]): Future[Int] =
    challengeDao.addChallenge(userId, challengeType, Util.b64EncodeToString(challenge))

  def findLoggedInUser(credentialId: String): Future[Option[User]] = for {
    maybeUserId <- credentialsDao.userIdByCredentialId(credentialId)
    user <- maybeUserId match {
      case Some(userId) => userDao.findById(userId)
      case None => Future.successful(None)
    }
  } yield user

  def addUser(username: String, password: String): Future[Long] =
    userDao.addUser(username,password)
}
