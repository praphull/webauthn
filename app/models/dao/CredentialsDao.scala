package models.dao

import com.google.inject.{Inject, Singleton}
import models.dao.CredentialsDao.{CredentialsRepo, credentialIdHash}
import models.{CredentialRegistrationResponse, ServerConfig, Credential => CredentialDTO}
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CredentialsDao @Inject()(server: ServerConfig) {
  //private val logger = Logger(this.getClass)
  private val query = CredentialsRepo.query

  def registerCredentials(userId: Long,
                          dto: CredentialDTO): Future[Either[String, CredentialRegistrationResponse]] = {
    server.run(for {
      exists <- query.filter(c => c.userId === userId && c.credentialIdHash === credentialIdHash(dto.id)).exists.result
      res <- if (exists) {
        DBIO.successful(Left("Credential already exists"))
      } else {
        (query += ((-1L, dto.id, credentialIdHash(dto.id), dto.credentialType, userId,
          if (dto.transportTypes.isEmpty) None else Some(dto.transportTypes.mkString(",")),
          Instant.now))).map { _ => //dto.publicKey,
          Right(CredentialRegistrationResponse(dto.id))
        }
      }
    } yield res)
  }

  def getRegisteredCredentials(userId: Long): Future[Seq[CredentialDTO]] = {
    server.run(query.filter(_.userId === userId).map { c =>
      (c.credentialType, c.credentialId, c.transportTypes)
    }.result).map(_.map { case (ct, cid, tt) =>
      CredentialDTO(ct, cid, tt.map(_.split(",").toSet).getOrElse(Set.empty))
    })
  }

  def userIdByCredentialId(credentialId: String): Future[Option[Long]] = {
    //FIXME Base64 encoding is messing up the end (most likely due to padding
    server.run(query.filter(_.credentialIdHash === credentialIdHash(credentialId)).map(_.userId).result.headOption)
  }

}

private object CredentialsDao {
  def credentialIdHash(c: String): String = c
    .replaceAll("""\+""", "")
    .replaceAll("""-""", "")
    .replaceAll("""_""", "")
    .replaceAll("""/""", "")

  case class Credential(id: Long, userId: Long, credentialType: String, credentialId: String,
                        credentialIdHash: String, transportTypes: Set[String]) { //publicKey: PublicKey,
    def toDTO: CredentialDTO = CredentialDTO(credentialType, credentialId,
      transportTypes)
  }

  class CredentialsRepo(tag: Tag)
    extends Table[(Long, String, String, String, Long, Option[String], Instant)](tag, "credentials") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def credentialId = column[String]("credential_id", O.Unique)

    def credentialIdHash = column[String]("credential_id_hash", O.Unique)

    def credentialType = column[String]("credential_type")

    def userId = column[Long]("user_id")

    def transportTypes = column[Option[String]]("transport_types")

    def createdAt = column[Instant]("created_at")

    override def * = (id, credentialId,
      credentialIdHash, credentialType, userId, transportTypes, createdAt)
  }

  object CredentialsRepo {
    val query = TableQuery[CredentialsRepo]
  }

}