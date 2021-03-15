package models

import javax.inject.Inject

@javax.inject.Singleton
class UserDao @Inject()() {
  private val users = Map(
    "foo" -> "bar",
    "foo1" -> "bar"
  )

  def lookupUser(u: User): Boolean = {
    //TODO query your database here
    users.get(u.username).contains(u.password)
  }

}