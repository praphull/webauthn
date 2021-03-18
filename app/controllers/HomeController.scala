package controllers

import com.typesafe.config.Config
import controllers.HomeController.AssetLinkTarget
import controllers.HomeController.AssetLinkTarget.ConfigOptWrapper
import models.FidoException
import play.api.Configuration
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.mvc._

import javax.inject._
import scala.jdk.CollectionConverters._

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               config: Configuration) extends AbstractController(cc) {

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(
      routes.UserController.showLoginForm(),
      routes.LandingPageController.showLandingPage()
    ))
  }

  def getAssetLinksJson() = Action {
    val links = config.underlying.getConfigList("webauthn.assetLinks").asScala.map { config =>
      val ns = config.getString("ns")
      if (ns.equals("web")) { //web
        AssetLinkTarget.web(config.get("site", _.getString))
      } else { //android
        AssetLinkTarget.android(
          config.get("pkg", _.getString),
          config.get("sha256", _.getStringList).asScala.toList
        )
      }
    }

    Ok(Json.toJson(links.map(_.defaultLink)))
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

    implicit class ConfigOptWrapper(val config: Config) {
      def opt[R](key: String, read: Config => String => R): Option[R] = {
        if (config.hasPath(key)) Some(read(config)(key)) else None
      }

      def get[R](key: String, read: Config => String => R): R = {
        opt(key, read).getOrElse(throw FidoException(s"Unable to find a value at $key"))
      }
    }

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
