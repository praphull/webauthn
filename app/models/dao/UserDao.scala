package models.dao

import models.{User => UserDTO}
import service.auth.PasswordManager

import javax.inject.Inject

@javax.inject.Singleton
class UserDao @Inject()() {

  import UserDao._

  private val users = List(
    User(1L, "foo", "bar"),
    User(2L, "foo1", "bar1"),
    User(3L, "foo2", "bar2"),
  ).map(u => u.username -> u.copy(password = PasswordManager.hashPassword(u.password))).toMap

  def findUser(username: String, password: String): Option[UserDTO] = {
    //TODO query database here
    users.get(username).flatMap { u =>
      if (PasswordManager.checkPassword(password, u.password)) Some(u.toDTO) else None
    }
  }

}

private object UserDao {

  case class User(userId: Long, username: String, password: String) {
    def toDTO: UserDTO = UserDTO(userId, username)
  }

}