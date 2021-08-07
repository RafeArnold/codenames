package uk.co.rafearnold.codenames

import io.vertx.core.Handler
import io.vertx.core.http.Cookie
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import uk.co.rafearnold.codenames.api.v1.handler.playerIdCookieName
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class WebPageHtmlRouteConfig @Inject constructor(
    route: Route,
    @Named("static-resources-dir") private val staticResourcesDirectory: String
) : Handler<RoutingContext>, Route by route {

    init {
        route
            .path("/")
            .handler(this)
    }

    override fun handle(ctx: RoutingContext) {
        if (ctx.getCookie(playerIdCookieName) == null) {
            ctx.addCookie(Cookie.cookie(playerIdCookieName, "${UUID.randomUUID()}"))
        }
        ctx.response()
            .sendFile("$staticResourcesDirectory/index.html")
    }
}
