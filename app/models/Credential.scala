package models

import com.webauthn4j.data.{AuthenticatorTransport, PublicKeyCredentialDescriptor, PublicKeyCredentialType}
import models.Credential.{CredentialType, TransportType}
import service.Util

import scala.jdk.CollectionConverters._

case class Credential(credentialType: String, id: String, transportTypes: Set[String]) {//publicKey: PublicKey,
  def getDescriptor: PublicKeyCredentialDescriptor = {
    //TODO: Verify how id is converted to bytes
    new PublicKeyCredentialDescriptor(
      CredentialType.from(credentialType).underlying,
      Util.b64Decode(id),
      transportTypes.map(t => TransportType.from(t).underlying).asJava
    )
  }
}

object Credential {

  sealed abstract class CredentialType(val underlying: PublicKeyCredentialType)

  object CredentialType {

    case object PublicKey extends CredentialType(PublicKeyCredentialType.PUBLIC_KEY)

    private val all: Set[CredentialType] = Set(PublicKey)

    def from(value: String): CredentialType =
      all.collectFirst { case c if c.underlying.getValue.equals(value) => c }
        .getOrElse(throw FidoException(s"Invalid credential type $value"))

  }

  sealed abstract class TransportType(val underlying: AuthenticatorTransport)

  object TransportType {

    case object BLE extends TransportType(AuthenticatorTransport.BLE)

    case object INTERNAL extends TransportType(AuthenticatorTransport.INTERNAL)

    case object NFC extends TransportType(AuthenticatorTransport.NFC)

    case object USB extends TransportType(AuthenticatorTransport.USB)

    private val all = Set(BLE, INTERNAL, NFC, USB)

    def from(value: String): TransportType =
      all.collectFirst { case t if t.underlying.getValue.equals(value) => t }
        .getOrElse(throw FidoException(s"Invalid transport type $value"))

  }

}
