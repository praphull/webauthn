package models

import models.FidoLoginRequest.AssertionResponse
import play.api.libs.json.{Json, Reads}

case class FidoLoginRequest(id: String, `type`: String, rawId: String, response: AssertionResponse)

object FidoLoginRequest {

  case class AssertionResponse(clientDataJSON: String, authenticatorData: String, signature: String, userHandle: String)

  object AssertionResponse {
    implicit val reads: Reads[AssertionResponse] = Json.reads[AssertionResponse]
  }

  implicit val reads: Reads[FidoLoginRequest] = Json.reads[FidoLoginRequest]
}
