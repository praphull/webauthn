package controllers

import com.fasterxml.jackson.databind.ObjectMapper
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import service.fido.FidoService

import javax.inject.{Inject, Singleton}

@Singleton
class AdminController @Inject()(cc: ControllerComponents,
                                fidoService: FidoService)
  extends AbstractController(cc) {
  private val logger = Logger(this.getClass)
  private val objectMapper = new ObjectMapper()

  def showCredentials = Action {
    val res = fidoService.getAllCredentials.map { case (id, userId, c) =>
      val cred = Json.parse(objectMapper.writeValueAsString(c.getDescriptor))
      Json.obj("id" -> id, "userId" -> userId, "credential" -> cred)
    }
    val json = Json.toJson(res)
    logger.info(s"showCredentials: $json")
    Ok(json)
  }
}