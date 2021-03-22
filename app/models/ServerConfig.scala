package models

import slick.jdbc.PostgresProfile.api._

trait ServerConfig {
  def rpId: String

  def origin: String

  def serverName: String

  def db: Database

  def adminToken: String
}
