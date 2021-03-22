package service.fido

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import com.webauthn4j.data._
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier
import com.webauthn4j.data.client.challenge.DefaultChallenge
import models.{ChallengeType, ServerConfig}
import play.api.Logger
import service.fido.ChallengeGenerator.{LoginTimeout, RegisterTimeout}

import java.nio.charset.StandardCharsets
import java.util.Collections
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

@Singleton
class ChallengeGenerator @Inject()(fidoConfig: ServerConfig,
                                   fidoService: FidoService) {
  private val logger = Logger(this.getClass)
  private val objectMapper = new ObjectMapper()

  def getRegistrationChallenge(userId: Long, userName: String,
                               platformAuthOnly: Boolean): Future[String] = {
    val rp = new PublicKeyCredentialRpEntity(fidoConfig.rpId, fidoConfig.serverName)
    val user = new PublicKeyCredentialUserEntity(
      userId.toString.getBytes(StandardCharsets.UTF_8), userName, userName)

    val challenge = new DefaultChallenge()

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
    fidoService.addChallenge(userId, ChallengeType.Registration, challenge.getValue).map { _ =>
      res
    }
  }

  def getLoginChallenge(userId: Long): Future[Option[String]] = {
    for {
      maybeAllowedCredentials <- fidoService.getRegisteredPublicKeys(userId)
      res <- maybeAllowedCredentials match {
        case Some(allowedCredentials) =>
          val challenge = new DefaultChallenge()

          val options = new PublicKeyCredentialRequestOptions(challenge, LoginTimeout,
            fidoConfig.rpId, allowedCredentials.map(_.getDescriptor).asJava,
            UserVerificationRequirement.PREFERRED, null)
          val res = objectMapper.writeValueAsString(options)
          logger.warn(s"getLoginChallenge: Generated options: $res")
          fidoService.addChallenge(userId, ChallengeType.Login, challenge.getValue).map { _ =>
            Some(res)
          }
        case None => Future.successful(None)
      }
    } yield res
  }
}

object ChallengeGenerator {
  val RegisterTimeout: Long = 15 * 1000L
  val LoginTimeout: Long = 15 * 1000L
}
