package modules

import com.google.inject.{AbstractModule, Inject, Provider, Singleton}
import models.ServerConfig
import modules.ServerConfigModule.ServerConfigProvider
import play.api.Configuration
import slick.jdbc.PostgresProfile.api._

@Singleton
class ServerConfigModule extends AbstractModule {
  override protected def configure(): Unit = {
    bind(classOf[ServerConfig]).toProvider(classOf[ServerConfigProvider])
  }
}

object ServerConfigModule {

  @Singleton
  private class ServerConfigProvider @Inject()(configuration: Configuration) extends Provider[ServerConfig] {
    override val get: ServerConfig = new ServerConfig {
      override lazy val rpId: String = configuration.get[String]("webauthn.server.rpId")
      override lazy val origin: String = configuration.get[String]("webauthn.server.origin")
      override val serverName: String = "WebAuthn Demo - Praphull"
      override lazy val db = Database.forConfig("fidodb")
      override lazy val adminToken: String = configuration.get[String]("webauthn.server.admin-token")
    }
  }

}
