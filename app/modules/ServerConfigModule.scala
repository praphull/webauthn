package modules

import com.google.inject.{AbstractModule, Inject, Provider, Singleton}
import models.FidoConfig
import modules.ServerConfigModule.FidoConfigProvider
import play.api.Configuration

@Singleton
class ServerConfigModule extends AbstractModule {
  override protected def configure(): Unit = {
    bind(classOf[FidoConfig]).toProvider(classOf[FidoConfigProvider])
  }
}

object ServerConfigModule {

  @Singleton
  private class FidoConfigProvider @Inject()(configuration: Configuration) extends Provider[FidoConfig] {
    override val get: FidoConfig = new FidoConfig {
      override lazy val rpId: String = configuration.get[String]("webauthn.server.rpId")
      override lazy val origin: String = configuration.get[String]("webauthn.server.origin")
      override val serverName: String = "WebAuthn Demo - Praphull"
    }
  }

}
