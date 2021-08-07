package uk.co.rafearnold.codenames.api.v1.handler

import io.vertx.core.Handler
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.ServerWebSocket
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.co.rafearnold.codenames.api.ApiException
import uk.co.rafearnold.codenames.api.v1.EventResponseHandlerV1
import uk.co.rafearnold.codenames.api.v1.GameConnectionServiceV1
import uk.co.rafearnold.codenames.api.v1.PlayerConnectionConfig
import uk.co.rafearnold.codenames.api.v1.model.GameEventRequestModelV1
import uk.co.rafearnold.codenames.api.v1.model.mapToGameEventRequest
import uk.co.rafearnold.codenames.api.v1.serializer.EventRequestSerializerV1
import uk.co.rafearnold.codenames.event.EventRequestHandler
import uk.co.rafearnold.codenames.event.GameEventResponse
import java.util.*
import javax.inject.Inject

class EventRouteHandler @Inject constructor(
    route: Route,
    private val eventRequestSerializer: EventRequestSerializerV1,
    private val eventRequestHandler: EventRequestHandler,
    private val eventResponseHandler: EventResponseHandlerV1,
    private val connectionService: GameConnectionServiceV1
) : Handler<RoutingContext>, Route by route {

    init {
        route
            .path("/play/v1/events")
            .handler(this)
    }

    override fun handle(ctx: RoutingContext) {
        runCatching {
            val serverRequest: HttpServerRequest = ctx.request()
            val playerId: String =
                serverRequest.getCookie(playerIdCookieName)?.value ?: "${UUID.randomUUID()}"
            log.info("Connection opened for player '$playerId' - method: ${serverRequest.method()}, path: ${serverRequest.path()}, query: ${serverRequest.query()}, body: ${ctx.bodyAsString}")
            val gameId: String? = ctx.queryParam(gameIdQueryParamName)?.firstOrNull()
            if (gameId == null) {
                val errorMessage = "No '$gameIdQueryParamName' query parameter provided"
                log.info(errorMessage)
                ctx.fail(400, ApiException(errorMessage))
                return
            }
            val webSocket: ServerWebSocket = serverRequest.upgrade()
            val connectionConfig =
                PlayerConnectionConfig(playerId = playerId, connection = webSocket)
            connectionService.addConnection(gameId = gameId, connectionConfig = connectionConfig)
            webSocket
                .textMessageHandler { message: String ->
                    runCatching {
                        log.info("Text message received from player '$playerId': $message")
                        val event: GameEventRequestModelV1 =
                            eventRequestSerializer.deserializeRequest(message)
                        val response: GameEventResponse =
                            eventRequestHandler.handle(
                                event.mapToGameEventRequest(gameId = gameId, playerId = playerId)
                            )
                        eventResponseHandler.handle(response)
                    }.onFailure {
                        log.error("Unexpected error handling text message", it)
                    }
                }
                .closeHandler {
                    runCatching {
                        log.info("Connection closed for player '$playerId'")
                        val event: GameEventRequestModelV1 =
                            GameEventRequestModelV1.RemovePlayer
                        val response: GameEventResponse =
                            eventRequestHandler.handle(
                                event.mapToGameEventRequest(gameId = gameId, playerId = playerId)
                            )
                        connectionService.removeConnection(gameId = gameId, playerId = playerId)
                        eventResponseHandler.handle(response)
                    }.onFailure {
                        log.error("Unexpected error handling connection closure", it)
                    }
                }
        }.onFailure {
            log.error("Unexpected error handling request", it)
            ctx.fail(500, ApiException("Internal Server Error"))
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EventRouteHandler::class.java)
        private const val gameIdQueryParamName: String = "game-id"
    }
}

const val playerIdCookieName: String = "codenames.player-id"
