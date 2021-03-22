package models.dao

import com.google.inject.{Inject, Singleton}
import models.{ServerConfig, User => UserDTO}
import service.auth.PasswordManager
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserDao @Inject()(server: ServerConfig) {

  import UserDao._

  private val query = UserRepo.query

  def findUser(username: String, password: String): Future[Option[UserDTO]] = {
    server.run(query.filter(_.phoneNumber === username).map { user =>
      (user.id, user.phoneNumber, user.password)
    }.result.headOption).map(_.flatMap { case (id, pn, pwd) =>
      if (PasswordManager.checkPassword(password, pwd)) Some(UserDTO(id, pn)) else None
    })
  }

  def findById(userId: Long): Future[Option[UserDTO]] =
    server.run(query.filter(_.id === userId).map { user =>
      (user.id, user.phoneNumber)
    }.result.headOption).map(_.map { case (id, pn) =>
      UserDTO(id, pn)
    })

  def findByUsername(username: String): Future[Option[UserDTO]] =
    server.run(query.filter(_.phoneNumber === username).map { user =>
      (user.id, user.phoneNumber)
    }.result.headOption).map(_.map { case (id, pn) =>
      UserDTO(id, pn)
    })

  def addUser(username: String, password: String): Future[Long] =
    server.run((query returning query.map(_.id)) += ((-1L, username,
      PasswordManager.hashPassword(password), Instant.now())))

}

private object UserDao {

  case class User(userId: Long, username: String, password: String) {
    def toDTO: UserDTO = UserDTO(userId, username)
  }

  class UserRepo(tag: Tag)
    extends Table[(Long, String, String, Instant)](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def phoneNumber = column[String]("phone_number", O.Unique)

    def password = column[String]("password_hash")

    def createdAt = column[Instant]("created_at")

    override def * = (id, phoneNumber, password, createdAt)
  }

  object UserRepo {
    val query = TableQuery[UserRepo]
  }

}