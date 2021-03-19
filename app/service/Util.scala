package service

import com.webauthn4j.util.Base64Util

object Util {
  def b64Decode(str: String): Array[Byte] = Base64Util.decode(str)

  def b64Encode(bytes: Array[Byte]): Array[Byte] = Base64Util.encode(bytes)

  def b64EncodeToString(bytes: Array[Byte]): String = Base64Util.encodeToString(bytes)
}
