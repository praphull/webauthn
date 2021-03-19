package models

import models.CredentialRegistrationRequest.AuthAttestationResponse
import play.api.libs.json.{Json, Reads}

case class CredentialRegistrationRequest(id: String, `type`: String, rawId: String,
                                         response: AuthAttestationResponse)

object CredentialRegistrationRequest {

  case class AuthAttestationResponse(clientDataJSON: String, attestationObject: String)

  object AuthAttestationResponse {
    implicit val reads: Reads[AuthAttestationResponse] = Json.reads[AuthAttestationResponse]
  }

  implicit val reads: Reads[CredentialRegistrationRequest] = Json.reads[CredentialRegistrationRequest]

}
