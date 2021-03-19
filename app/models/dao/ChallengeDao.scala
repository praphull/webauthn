package models.dao

import com.google.inject.Singleton
import models.ChallengeType
import models.dao.ChallengeDao.Challenge

@Singleton
class ChallengeDao {
  private var challenges = List.empty[Challenge]

  def addChallenge(userId: Long, challengeType: ChallengeType, challenge: String): Unit = {
    val c = Challenge(challenges.length + 1, userId, challengeType, challenge)
    challenges = c :: challenges
  }
}

private object ChallengeDao {

  case class Challenge(challengeId: Long, userId: Long, challengeType: ChallengeType,
                       challenge: String)

}
