package models

import play.api.libs.json.{Json, Reads}

case class UserLoginRequest(username: String, password: String)

object UserLoginRequest {
  implicit val reads: Reads[UserLoginRequest] = Json.reads[UserLoginRequest]
}
