package uk.co.rafearnold.codenames

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.ext.web.Router
import javax.inject.Inject
import javax.inject.Named

class ServerVerticle @Inject constructor(
    private val router: Router,
    @Named("server.port") private val serverPort: Int
) : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(serverPort) { startPromise.complete() }
    }
}
