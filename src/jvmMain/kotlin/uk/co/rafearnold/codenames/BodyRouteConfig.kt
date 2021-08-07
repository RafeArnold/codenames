package uk.co.rafearnold.codenames

import io.vertx.ext.web.Route
import io.vertx.ext.web.handler.BodyHandler
import javax.inject.Inject

class BodyRouteConfig @Inject constructor(route: Route) : Route by route {

    init {
        route.handler(BodyHandler.create(false))
    }
}
