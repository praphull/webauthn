package service.fido

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import com.webauthn4j.data._
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.challenge.DefaultChallenge
import models.{FidoConfig, FidoException}
import service.fido.ChallengeGenerator.{LoginTimeout, RegisterTimeout}

import java.nio.charset.StandardCharsets
import java.util.Collections
import scala.jdk.CollectionConverters._

@Singleton
class ChallengeGenerator @Inject()(fidoConfig: FidoConfig,
                                   credentialsService: CredentialsService) {
  private val objectMapper = new ObjectMapper()

  def getRegistrationChallenge(userId: String, userName: String,
                               platformAuthOnly: Boolean): Array[Byte] = {
    val rp = new PublicKeyCredentialRpEntity(fidoConfig.serverId, fidoConfig.serverName)
    val user = new PublicKeyCredentialUserEntity(
      userId.getBytes(StandardCharsets.UTF_8), userName, userName)

    val challenge = new DefaultChallenge()

    val params = List(new PublicKeyCredentialParameters(
      PublicKeyCredentialType.PUBLIC_KEY,
      COSEAlgorithmIdentifier.ES256
    )).asJava

    val authenticatorAttachment = if (platformAuthOnly) AuthenticatorAttachment.PLATFORM else AuthenticatorAttachment.CROSS_PLATFORM

    val authCriteria = new AuthenticatorSelectionCriteria(authenticatorAttachment,
      ResidentKeyRequirement.PREFERRED, UserVerificationRequirement.PREFERRED)

    val options = new PublicKeyCredentialCreationOptions(rp, user, challenge, params,
      RegisterTimeout, Collections.emptyList(), authCriteria, null, null)
    objectMapper.writeValueAsBytes(options)
  }

  def getLoginChallenge(username: String): Array[Byte] = {
    val challenge = new DefaultChallenge()
    val allowedCredentials = credentialsService.getRegisteredPublicKeys(username)
      .getOrElse(throw FidoException("Invalid username"))

    val options = new PublicKeyCredentialRequestOptions(challenge, LoginTimeout,
      fidoConfig.serverId, allowedCredentials.map(_.getDescriptor).asJava,
      UserVerificationRequirement.PREFERRED, null)
    objectMapper.writeValueAsBytes(options)

  }
}

object ChallengeGenerator {
  val RegisterTimeout: Long = 15 * 1000L
  val LoginTimeout: Long = 15 * 1000L
}
