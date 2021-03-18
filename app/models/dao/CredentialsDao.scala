package models.dao

import com.google.inject.Singleton
import models.dao.CredentialsDao.Credential
import models.{Credential => CredentialDTO}

@Singleton
class CredentialsDao {
  private var credentials = List.empty[Credential]

  def registerCredentials(userId: Long, dto: CredentialDTO): Either[String, Long] = {
    if (credentials.exists(c => c.userId == userId && c.credentialId == dto.id)) {
      Left("Credential already exist")
    } else {
      val cred = Credential(credentials.length + 1, userId, dto.credentialType, dto.id, dto.transportTypes)
      credentials = cred :: credentials
      Right(cred.id)
    }
  }

  def getRegisteredPublicKeys(userId: Long): List[CredentialDTO] = {
    credentials.filter(_.userId == userId).map(_.toDTO)
  }

}

private object CredentialsDao {

  case class Credential(id: Long, userId: Long, credentialType: String, credentialId: String, transportTypes: String) {
    def toDTO: CredentialDTO = CredentialDTO(credentialType, credentialId, transportTypes)
  }

}