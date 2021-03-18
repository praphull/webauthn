package service.fido

import com.google.inject.{Inject, Singleton}
import models.Credential
import models.dao.{CredentialsDao, UserDao}

@Singleton
class FidoService @Inject()(credentialsDao: CredentialsDao,
                            userDao: UserDao) {
  def getRegisteredPublicKeys(username: String): Option[List[Credential]] = for {
    user <- userDao.findByUsername(username)
  } yield credentialsDao.getRegisteredPublicKeys(user.userId)

  def getRegisteredPublicKeys(userId: Long): Option[List[Credential]] = for {
    user <- userDao.findById(userId)
  } yield credentialsDao.getRegisteredPublicKeys(user.userId)

  def getUserId(username: String): Option[Long] =
    userDao.findByUsername(username).map(_.userId)
}
