package service.fido

import com.google.inject.{Inject, Singleton}
import models.Credential
import models.dao.{CredentialsDao, UserDao}

@Singleton
class CredentialsService @Inject()(credentialsDao: CredentialsDao,
                                   userDao: UserDao) {
  def getRegisteredPublicKeys(username: String): Option[List[Credential]] = for {
    user <- userDao.findByUsername(username)
  } yield credentialsDao.getRegisteredPublicKeys(user.userId)

}
