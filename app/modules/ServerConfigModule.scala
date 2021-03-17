package modules

import com.google.inject.{AbstractModule, Provider, Singleton}
import models.FidoConfig
import modules.ServerConfigModule.FidoConfigProvider

@Singleton
class ServerConfigModule extends AbstractModule {
  override protected def configure(): Unit = {
    bind(classOf[FidoConfig]).toProvider(classOf[FidoConfigProvider])
  }
}
object ServerConfigModule {
  @Singleton
  private class FidoConfigProvider extends Provider[FidoConfig] {
    override val get: FidoConfig = new FidoConfig {
      override val serverId: String = "https://fido2.apps.praphull.com"

      override val serverName: String = "WebAuthn Demo - Praphull"
    }
  }
}
