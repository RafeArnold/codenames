package uk.co.rafearnold.codenames

import io.vertx.core.Handler
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject
import javax.inject.Named

class WebPageJsRouteConfig @Inject constructor(
    route: Route,
    @Named("static-resources-dir") private val staticResourcesDirectory: String
) : Handler<RoutingContext>, Route by route {

    init {
        route
            .path("/$jsFilename")
            .handler(this)
    }

    override fun handle(ctx: RoutingContext) {
        ctx.response().sendFile("$staticResourcesDirectory/$jsFilename")
    }

    companion object {
        private const val jsFilename: String = "codenames.js"
    }
}
