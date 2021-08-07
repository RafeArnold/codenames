package uk.co.rafearnold.codenames.api.v1.handler

import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.codenames.api.ApiException
import uk.co.rafearnold.codenames.api.v1.model.NewGameResponseModelV1
import uk.co.rafearnold.codenames.api.v1.serializer.NewGameResponseSerializerV1
import uk.co.rafearnold.codenames.game.Game
import uk.co.rafearnold.codenames.game.GamesService
import javax.inject.Inject

class NewGameRouteHandler @Inject constructor(
    route: Route,
    private val responseSerializer: NewGameResponseSerializerV1,
    private val gamesService: GamesService
) : Handler<RoutingContext>, Route by route {

    init {
        route.method(HttpMethod.POST)
            .path("/play/v1/new-game")
            .handler(this)
    }

    override fun handle(ctx: RoutingContext) {
        runCatching {
            val serverRequest: HttpServerRequest = ctx.request()
            log.info("New game request received - method: ${serverRequest.method()}, path: ${serverRequest.path()}, query: ${serverRequest.query()}, body: ${ctx.bodyAsString}")
            val game: Game = gamesService.newGame()
            val gameId: String = game.id
            val response = NewGameResponseModelV1(gameId = gameId)
            ctx.response()
                .setStatusCode(HttpResponseStatus.OK.code())
                .end(responseSerializer.serialize(response))
        }.onFailure {
            log.error("Unexpected error occurred", it)
            ctx.fail(500, ApiException("Internal Server Error"))
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(NewGameRouteHandler::class.java)
    }
}
