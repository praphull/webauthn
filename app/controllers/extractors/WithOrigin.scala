package controllers.extractors

import com.webauthn4j.data.client.Origin
import play.api.mvc.Request

object WithOrigin {
  def apply[A, R](f: Origin => R)
                 (implicit request: Request[_]): R = {
    f(new Origin(s"${if (request.secure) "https" else "http"}${request.host}"))
  }

}
