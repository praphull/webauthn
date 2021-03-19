package models

trait FidoConfig {
  def rpId: String

  def origin: String

  def serverName: String
}
