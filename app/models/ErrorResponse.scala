package models

import play.api.libs.json.{JsValue, Json, OWrites}

case class ErrorResponse(code: Int, message: String) {
  lazy val json: JsValue = Json.toJson(this)
}

object ErrorResponse {
  implicit val writes: OWrites[ErrorResponse] = Json.writes[ErrorResponse]

  val InvalidLoginRequest: ErrorResponse = ErrorResponse(4001, "Invalid login request")

  val NotLoggedIn: ErrorResponse = ErrorResponse(4031, "User not logged in!")
  val InvalidToken: ErrorResponse = ErrorResponse(4032, "Token is invalid!")
}
