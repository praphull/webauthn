package service

import scala.annotation.tailrec

object Validators {

  sealed trait Validated[R] {
    def isValid: Boolean

    def result: Either[(String, String), Unit] = this match {
      case Validated.Valid(_) => Right(())
      case Validated.Invalid(error, fieldAndLabel) => Left(fieldAndLabel._2 -> error)
    }
  }

  object Validated {

    case class Valid[R](fieldAndLabel: (R, String)) extends Validated[R] {
      override def isValid: Boolean = true
    }

    case class Invalid[R](error: String, fieldAndLabel: (R, String)) extends Validated[R] {
      override def isValid: Boolean = false
    }

  }

  private def doValidation[R](fieldAndLabel: (R, String), error: => String, condition: R => Boolean): Validated[R] =
    if (condition(fieldAndLabel._1)) Validated.Valid(fieldAndLabel) else Validated.Invalid(error, fieldAndLabel)


  implicit class Validator[R](val fieldAndLabel: (R, String)) {
    def verifying(error: => String, condition: R => Boolean): Validated[R] =
      doValidation(fieldAndLabel, error, condition)
  }

  implicit class ValidatorChain[R](val validated: Validated[R]) {

    def verifying(error: => String, condition: R => Boolean): Validated[R] = validated match {
      case Validated.Valid(fieldAndLabel) => doValidation(fieldAndLabel, error, condition)
      case error: Validated.Invalid[R] => error
    }
  }

  @tailrec
  def validate(validations: List[Either[(String, String), Unit]]): Either[(String, String), Unit] =
    validations match {
      case Nil => Right(())
      case Left(labelAndError) :: _ => Left(labelAndError)
      case Right(_) :: rest => validate(rest)
    }

}
