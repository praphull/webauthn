package controllers

import akka.util.ByteString
import com.webauthn4j.WebAuthnManager
import com.webauthn4j.converter.AttestationObjectConverter
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.data.RegistrationRequest
import com.webauthn4j.validator.attestation.statement.androidsafetynet.AndroidSafetyNetAttestationStatementValidator
import com.webauthn4j.validator.attestation.statement.tpm.TPMAttestationStatementValidator
import com.webauthn4j.validator.attestation.trustworthiness.certpath.NullCertPathTrustworthinessValidator
import com.webauthn4j.validator.attestation.trustworthiness.self.NullSelfAttestationTrustworthinessValidator
import controllers.FidoController.ParamPlatformAuthenticatorOnly
import models._
import play.api.Logger
import play.api.http.HttpEntity
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import service.Util
import service.fido.{ChallengeGenerator, FidoService}

import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.util.Try

@Singleton
class FidoController @Inject()(cc: ControllerComponents,
                               authenticatedUserAction: AuthenticatedUserAction,
                               challengeGenerator: ChallengeGenerator,
                               fidoService: FidoService)
  extends AbstractController(cc) {
  private val logger = Logger(this.getClass)

  private val objectConverter = new ObjectConverter()
  private val webAuthnManager = new WebAuthnManager(
    util.Arrays.asList(
      new AndroidSafetyNetAttestationStatementValidator,
      new TPMAttestationStatementValidator
    ),
    new NullCertPathTrustworthinessValidator,
    new NullSelfAttestationTrustworthinessValidator,
    objectConverter
  )

  def getRegistrationChallenge() = authenticatedUserAction { implicit request =>
    logger.info("getRegistrationChallenge: Inside")
    val restrictPlatformAuthenticatorOnly = request
      .getQueryString(ParamPlatformAuthenticatorOnly).exists(_.toBoolean)

    val options = challengeGenerator.getRegistrationChallenge(
      request.userId,
      request.username,
      restrictPlatformAuthenticatorOnly
    )

    Ok.sendEntity(HttpEntity.Strict(ByteString(options.getBytes), Some("application/json")))
  }

  def getUserId() = Action { request =>
    logger.info("getUserId: Inside")
    request.headers.get(Constants.HeaderUsernameKey).flatMap { userName =>
      fidoService.getUserId(userName)
    } match {
      case Some(userId) => Ok(Json.obj("userId" -> userId))
      case None => NotFound(ErrorResponse.InvalidUsername.json)
    }
  }

  def register() = authenticatedUserAction(parse.tolerantJson) { request =>
    logger.info(s"register: Inside. Body: ${request.body.toString()}")
    request.body.asOpt[CredentialRegistrationRequest] match {
      case Some(regRequest) =>
        val attestationObjectConverter = new AttestationObjectConverter(objectConverter)

        val clientDataJSON = Util.b64Decode(regRequest.response.clientDataJSON)

        val attestationObject = attestationObjectConverter.convert(regRequest.response.attestationObject)
        val attestationObjectBytes = attestationObjectConverter.convertToBytes(attestationObject)

        val registrationRequest = new RegistrationRequest(attestationObjectBytes, clientDataJSON)

        val registrationData = webAuthnManager.parse(registrationRequest)

        val credData = registrationData.getAttestationObject.getAuthenticatorData.getAttestedCredentialData

        val cred = Credential(regRequest.`type`,
          Util.b64EncodeToString(credData.getCredentialId),
          credData.getCOSEKey.getPublicKey,
          if (registrationData.getTransports != null) registrationData.getTransports.asScala.map(_.getValue).toSet else Set.empty
        )

        fidoService.registerCredentials(request.userId, cred) match {
          case Left(error) => BadRequest(ErrorResponse.registrationFailed(error).json)
          case Right(credential) =>
            val json = Json.obj("credentials" -> Json.toJson(List(credential)))
            logger.info(s"register: returning: $json")
            Ok(json)
        }
      case None =>
        BadRequest(ErrorResponse.InvalidRegistrationRequest.json)
    }
  }

  def getLoginChallenge() = Action { request =>
    logger.info(s"getLoginChallenge: Inside: ${request.body.asText}")
    Try(request.headers.get(Constants.HeaderUserIdKey).map(_.toLong)).toOption.flatten match {
      case None => NotFound(ErrorResponse.InvalidUserId.json)
      case Some(userId) => challengeGenerator.getLoginChallenge(userId) match {
        case None => BadRequest(ErrorResponse.InvalidUserId.json)
        case Some(options) =>
          logger.info(s"getLoginChallenge: returning: $options")
          Ok.sendEntity(HttpEntity.Strict(ByteString(options.getBytes), Some("application/json")))
      }
    }
  }

  def login() = Action(parse.tolerantJson) { request =>
    logger.info(s"login: Inside: ${request.body}")
    val res = request.body.asOpt[FidoLoginRequest].flatMap { loginRequest =>
      //Fixme: Hack for now
      fidoService.findLoggedInUser(loginRequest.id)
    }
    res match {
      case Some(user) =>
        logger.info(s"login: User logged in: $user")
        Ok(Json.toJson(user))
      case None =>
        logger.warn(s"login: User not found")
        BadRequest(ErrorResponse.InvalidLoginRequest.json)
    }
  }

}

object FidoController {
  val ParamPlatformAuthenticatorOnly = "platform_only"
}
