package models

sealed trait ChallengeType {
  def asString: String
}

object ChallengeType {

  case object Registration extends ChallengeType {
    override val asString: String = "R"
  }

  case object Login extends ChallengeType {
    override val asString: String = "L"
  }

}
