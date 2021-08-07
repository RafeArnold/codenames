package uk.co.rafearnold.codenames

import io.netty.handler.codec.http.HttpHeaderNames
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Route
import io.vertx.ext.web.handler.CorsHandler
import javax.inject.Inject

class CorsRouteConfig @Inject constructor(route: Route) : Route by route {

    init {
        route
            .handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethods(allowedMethods))
    }

    companion object {
        private val allowedHeaders: Set<String> = setOf(
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(),
            HttpHeaderNames.ORIGIN.toString(),
            HttpHeaderNames.CONTENT_TYPE.toString(),
            HttpHeaderNames.ACCEPT.toString()
        )

        private val allowedMethods: Set<HttpMethod> = setOf(
            HttpMethod.GET,
            HttpMethod.POST,
            HttpMethod.DELETE,
            HttpMethod.PATCH,
            HttpMethod.OPTIONS,
            HttpMethod.PUT
        )
    }
}
