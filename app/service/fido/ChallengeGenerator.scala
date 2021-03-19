package service.fido

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import com.webauthn4j.data._
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.challenge.DefaultChallenge
import models.{ChallengeType, FidoConfig}
import play.api.Logger
import service.fido.ChallengeGenerator.{LoginTimeout, RegisterTimeout}

import java.nio.charset.StandardCharsets
import java.util.Collections
import scala.jdk.CollectionConverters._

@Singleton
class ChallengeGenerator @Inject()(fidoConfig: FidoConfig,
                                   fidoService: FidoService) {
  private val logger = Logger(this.getClass)
  private val objectMapper = new ObjectMapper()

  def getRegistrationChallenge(userId: Long, userName: String,
                               platformAuthOnly: Boolean): String = {
    val rp = new PublicKeyCredentialRpEntity(fidoConfig.rpId, fidoConfig.serverName)
    val user = new PublicKeyCredentialUserEntity(
      userId.toString.getBytes(StandardCharsets.UTF_8), userName, userName)

    val challenge = new DefaultChallenge()

    fidoService.addChallenge(userId, ChallengeType.Registration, challenge.getValue)

    val params = List(new PublicKeyCredentialParameters(
      PublicKeyCredentialType.PUBLIC_KEY,
      COSEAlgorithmIdentifier.ES256
    )).asJava

    //val authenticatorAttachment = if (platformAuthOnly) AuthenticatorAttachment.PLATFORM else AuthenticatorAttachment.CROSS_PLATFORM
    val authenticatorAttachment = AuthenticatorAttachment.PLATFORM

    val authCriteria = new AuthenticatorSelectionCriteria(authenticatorAttachment,
      ResidentKeyRequirement.DISCOURAGED, UserVerificationRequirement.DISCOURAGED)

    val options = new PublicKeyCredentialCreationOptions(rp, user, challenge, params,
      RegisterTimeout, Collections.emptyList(), authCriteria, null, null)
    val res = objectMapper.writeValueAsString(options)
    logger.warn(s"getRegistrationChallenge: Generated options: $res")
    res
  }

  def getLoginChallenge(userId: Long): Option[String] = {
    fidoService.getRegisteredPublicKeys(userId).map { allowedCredentials =>
      val challenge = new DefaultChallenge()

      fidoService.addChallenge(userId, ChallengeType.Login, challenge.getValue)

      val options = new PublicKeyCredentialRequestOptions(challenge, LoginTimeout,
        fidoConfig.rpId, allowedCredentials.map(_.getDescriptor).asJava,
        UserVerificationRequirement.PREFERRED, null)
      val res = objectMapper.writeValueAsString(options)
      logger.warn(s"getLoginChallenge: Generated options: $res")
      res
    }
  }
}

object ChallengeGenerator {
  val RegisterTimeout: Long = 15 * 1000L
  val LoginTimeout: Long = 15 * 1000L
}
