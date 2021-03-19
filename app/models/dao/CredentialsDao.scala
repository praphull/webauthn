package models.dao

import com.google.inject.Singleton
import models.dao.CredentialsDao.Credential
import models.{CredentialRegistrationResponse, Credential => CredentialDTO}
import play.api.Logger

import java.security.PublicKey

@Singleton
class CredentialsDao {
  private val logger = Logger(this.getClass)
  private var credentials = List.empty[Credential]

  def registerCredentials(userId: Long,
                          dto: CredentialDTO): Either[String, CredentialRegistrationResponse] = {
    if (credentials.exists(c => c.userId == userId && c.credentialId == dto.id)) {
      Left("Credential already exists")
    } else {
      val cred = Credential(credentials.length + 1, userId, dto.credentialType, dto.id,
        dto.publicKey, dto.transportTypes)
      credentials = cred :: credentials
      Right(CredentialRegistrationResponse(cred.credentialId))
    }
  }

  def getRegisteredPublicKeys(userId: Long): List[CredentialDTO] = {
    credentials.filter(_.userId == userId).map(_.toDTO)
  }

  def getAllCredentials(): List[(Long, Long, CredentialDTO)] = credentials.map { c =>
    (c.id, c.userId, c.toDTO)
  }

  //TODO: Fix
  def userIdByCredentialId(credentialId: String) = {
    //FIXME Base64 encoding is messing up the end (most likely due to padding
    val x = credentialId
      .replaceAll("""\+""", "")
      .replaceAll("""-""", "")
      .replaceAll("""_""", "")
      .replaceAll("""/""", "")
    val x1 = x.substring(0, x.length - 4)
    val y = credentials.head.credentialId
      .replaceAll("""\+""", "")
      .replaceAll("""-""", "")
      .replaceAll("""_""", "")
      .replaceAll("""/""", "")
      .substring(0, x1.length)
    logger.info(s"userIdByCredentialId: x: $x1")
    logger.info(s"userIdByCredentialId: y: $y")
    credentials.find(_.matchesCredId(x)).map(_.userId)
  }

}

private object CredentialsDao {

  case class Credential(id: Long, userId: Long, credentialType: String, credentialId: String,
                        publicKey: PublicKey, transportTypes: Set[String]) {
    def matchesCredId(credId: String) = credentialId
      .replaceAll("""\+""", "")
      .replaceAll("""-""", "")
      .replaceAll("""_""", "")
      .replaceAll("""/""", "")
      .substring(0, credId.length)
      .startsWith(credId)

    def toDTO: CredentialDTO = CredentialDTO(credentialType, credentialId, publicKey, transportTypes)
  }

}