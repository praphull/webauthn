package models.dao

import com.google.inject.{Inject, Singleton}
import models.{ChallengeType, ServerConfig}
import models.dao.ChallengeDao.ChallengeRepo
import slick.jdbc.PostgresProfile.api._

import java.time.Instant
import scala.concurrent.Future

@Singleton
class ChallengeDao @Inject()(server: ServerConfig) {
  private val query = ChallengeRepo.query

  def addChallenge(userId: Long, challengeType: ChallengeType, challenge: String): Future[Int] = {
    server.run(query += ((-1L, userId, challengeType.asString, challenge, Instant.now)))
  }
}

private object ChallengeDao {

  case class Challenge(challengeId: Long, userId: Long, challengeType: ChallengeType,
                       challenge: String)

  class ChallengeRepo(tag: Tag)
    extends Table[(Long, Long, String, String, Instant)](tag, "challenges") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def userId = column[Long]("user_id")

    def challengeType = column[String]("challenge_type")

    def challenge = column[String]("challenge")

    def createdAt = column[Instant]("created_at")

    override def * = (id, userId, challengeType, challenge, createdAt)
  }

  object ChallengeRepo {
    val query = TableQuery[ChallengeRepo]
  }

}
