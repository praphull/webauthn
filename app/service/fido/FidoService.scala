package service.fido

import com.google.inject.{Inject, Singleton}
import models.dao.{ChallengeDao, CredentialsDao, UserDao}
import models.{ChallengeType, Credential, CredentialRegistrationResponse, User}
import service.Util

@Singleton
class FidoService @Inject()(credentialsDao: CredentialsDao,
                            userDao: UserDao,
                            challengeDao: ChallengeDao) {
  def getRegisteredPublicKeys(username: String): Option[List[Credential]] = for {
    user <- userDao.findByUsername(username)
  } yield credentialsDao.getRegisteredPublicKeys(user.userId)

  def getRegisteredPublicKeys(userId: Long): Option[List[Credential]] = for {
    user <- userDao.findById(userId)
  } yield credentialsDao.getRegisteredPublicKeys(user.userId)

  def getUserId(username: String): Option[Long] =
    userDao.findByUsername(username).map(_.userId)

  def registerCredentials(userId: Long,
                          dto: Credential): Either[String, CredentialRegistrationResponse] =
    credentialsDao.registerCredentials(userId, dto)

  def addChallenge(userId: Long, challengeType: ChallengeType,
                   challenge: Array[Byte]): Unit =
    challengeDao.addChallenge(userId, challengeType, Util.b64EncodeToString(challenge))

  def getAllCredentials: List[(Long, Long, Credential)] = credentialsDao.getAllCredentials()

  def findLoggedInUser(credentialId: String): Option[User] = for {
    userId <- credentialsDao.userIdByCredentialId(credentialId)
    user <- userDao.findById(userId)
  } yield user
}
