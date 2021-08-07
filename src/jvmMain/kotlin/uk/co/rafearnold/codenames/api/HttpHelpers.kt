package uk.co.rafearnold.codenames.api

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import uk.co.rafearnold.codenames.CodeNamesException

val requestFailureHandler: Handler<RoutingContext> =
    Handler { ctx: RoutingContext ->
        ctx.response()
            .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .setStatusCode(ctx.statusCode())
            .end(
                JsonObject()
                    .put("cause", ctx.failure()?.message ?: "Invalid Request")
                    .put("method", ctx.request().method())
                    .put("path", ctx.normalisedPath())
                    .encodePrettily()
            )
    }

class ApiException(override val message: String) : CodeNamesException(message)
