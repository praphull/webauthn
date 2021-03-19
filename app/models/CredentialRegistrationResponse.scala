package models

import play.api.libs.json.{Json, OWrites}

case class CredentialRegistrationResponse(credId: String)

object CredentialRegistrationResponse {

  implicit val writes: OWrites[CredentialRegistrationResponse] = Json.writes[CredentialRegistrationResponse]

}