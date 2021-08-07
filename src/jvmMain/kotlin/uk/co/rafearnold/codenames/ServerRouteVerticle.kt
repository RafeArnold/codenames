package uk.co.rafearnold.codenames

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Route
import javax.inject.Inject

class ServerRouteVerticle @Inject constructor(
    private val routes: Set<@JvmSuppressWildcards Route>
) : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        for (route: Route in routes) {
            route.enable()
        }
        startPromise.complete()
    }
}
