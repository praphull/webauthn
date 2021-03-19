package service.auth

import service.Util

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

//Directly taken from https://stackoverflow.com/questions/18262425/how-to-hash-password-in-play-framework-maybe-with-bcrypt
object PasswordManager {
  val DefaultIterations = 1000
  private val random = new SecureRandom()

  private def pbkdf2(password: String, salt: Array[Byte], iterations: Int): Array[Byte] = {
    val keySpec = new PBEKeySpec(password.toCharArray, salt, iterations, 256)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    keyFactory.generateSecret(keySpec).getEncoded
  }

  def hashPassword(password: String): String = {
    val salt = new Array[Byte](16)
    random.nextBytes(salt)
    val hash = pbkdf2(password, salt, DefaultIterations)
    val salt64 = Base64.getEncoder.encodeToString(salt)
    val hash64 = Base64.getEncoder.encodeToString(hash)

    s"$DefaultIterations:$hash64:$salt64"
  }

  def checkPassword(password: String, passwordHash: String): Boolean = {
    passwordHash.split(":") match {
      case Array(it, hash64, salt64) if it.forall(_.isDigit) =>
        val hash = Util.b64Decode(hash64)
        val salt = Util.b64Decode(salt64)

        val calculatedHash = pbkdf2(password, salt, it.toInt)
        calculatedHash.sameElements(hash)

      case _ => false
    }
  }
}
