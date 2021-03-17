package models

import play.api.libs.json.{Json, OWrites}

case class User(userId: Long, username: String)

object User {
  implicit val writes: OWrites[User] = Json.writes[User]
}