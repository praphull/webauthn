package controllers

import controllers.HomeController.AssetLinkTarget
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.mvc._

import javax.inject._

@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(
      routes.UserController.showLoginForm(),
      routes.LandingPageController.showLandingPage()
    ))
  }

  def getAssetLinksJson() = Action {

    //TODO
    Ok(Json.toJson(List(
      AssetLinkTarget.web("https://fido2.apps.praphull.com"),
      AssetLinkTarget.android("com.praphull.experiments.fido", List())
    ).map(_.defaultLink)))
  }
}

object HomeController {

  case class AssetLinkTarget(namespace: String,
                             site: Option[String],
                             package_name: Option[String],
                             sha256_cert_fingerprints: Option[List[String]]) {
    def defaultLink: AssetLink = AssetLink(AssetLink.defaultRelations, this)
  }

  object AssetLinkTarget {
    def web(site: String): AssetLinkTarget = AssetLinkTarget("web", Some(site), None, None)

    def android(packageName: String, fingerprints: List[String]): AssetLinkTarget =
      AssetLinkTarget("android_app", None, Some(packageName), Some(fingerprints))

    implicit val writes: OWrites[AssetLinkTarget] = Json.writes[AssetLinkTarget]
  }

  case class AssetLink(relation: List[String], target: AssetLinkTarget) {
    def json: JsValue = Json.toJson(this)
  }

  object AssetLink {
    implicit val writes: OWrites[AssetLink] = Json.writes[AssetLink]
    val defaultRelations = List(
      "delegate_permission/common.handle_all_urls",
      "delegate_permission/common.get_login_creds"
    )
  }

}
